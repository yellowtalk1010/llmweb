import { useEffect, useRef, useState } from "react";
import {
  createChart,
  CrosshairMode,
  CandlestickSeries,
  HistogramSeries,
  createSeriesMarkers,
} from "lightweight-charts";

/***
 * 股票蜡烛图组件
 * 调用 /klineChart/list?stockCode=xxx&tradedate=xxx 接口
 * 主图：蜡烛图；副图：成交量柱状图；悬停显示 名称 + OHLC + vol + change + amount + winChange
 * optType 非空的蜡烛：上下交替标记（下方向上箭头 / 上方向下箭头）
 * A 股配色：涨红跌绿
 */
function KLineChart({ tscode, tradedate, height = 280 }) {
  console.info("tscode:" + tscode + ", tradedate:" + tradedate);

  const containerRef = useRef(null);
  const chartRef = useRef(null);
  const candleSeriesRef = useRef(null);
  const volumeSeriesRef = useRef(null);
  const markersPluginRef = useRef(null);

  const dataMapRef = useRef({});

  const [status, setStatus] = useState("loading");
  const [errorMsg, setErrorMsg] = useState("");
  const [hoverInfo, setHoverInfo] = useState(null);

  // A 股配色常量
  const upColor = "#ef5350";   // 红 = 涨
  const downColor = "#26a69a"; // 绿 = 跌

  // 让全部数据居中显示，左侧留少量边距，右侧留更多边距
  // count：蜡烛总数
  const centerAllData = (count) => {
    const chart = chartRef.current;
    if (!chart || !count) return;

    const width = containerRef.current
      ? containerRef.current.clientWidth
      : chart.timeScale().width();

    // 左侧留 8%，右侧留 16%（右侧更多）
    const leftRatio = 0.08;
    const rightRatio = 0.16;
    const usableWidth = Math.max(width * (1 - leftRatio - rightRatio), 1);

    // ↓↓↓ 再缩一点：用 0.6 的系数把蜡烛间距压得更小，下限降到 1
    const rawSpacing = (usableWidth / Math.max(count - 1, 1)) * 0.6;
    const barSpacing = Math.min(Math.max(rawSpacing, 1), 20);

    chart.timeScale().applyOptions({ barSpacing });

    // 左右按像素换算成逻辑空槽位
    const leftPaddingBars = Math.round((width * leftRatio) / barSpacing);
    const rightPaddingBars = Math.round((width * rightRatio) / barSpacing);

    chart.timeScale().setVisibleLogicalRange({
      from: -leftPaddingBars,
      to: count - 1 + rightPaddingBars,
    });
  };

  // 初始化图表
  useEffect(() => {
    if (!containerRef.current) return;

    const chart = createChart(containerRef.current, {
      width: containerRef.current.clientWidth,
      height,
      layout: {
        background: { color: "#1e1e2f" },
        textColor: "#d1d4dc",
      },
      grid: {
        vertLines: { color: "#2b2b3d" },
        horzLines: { color: "#2b2b3d" },
      },
      crosshair: { mode: CrosshairMode.Normal },
      timeScale: {
        borderColor: "#485c7b",
        timeVisible: true,
        tickMarkFormatter: (time) => time,
        minBarSpacing: 1,     // ↓↓↓ 下限再放宽，允许缩到最小
        maxBarSpacing: 20,
        rightOffset: 12,
        fixLeftEdge: false,
        fixRightEdge: false,
      },
      rightPriceScale: {
        borderColor: "#485c7b",
      },
    });

    // 主图：蜡烛图（A 股：涨红跌绿）
    const candleSeries = chart.addSeries(CandlestickSeries, {
      upColor: upColor,
      downColor: downColor,
      borderVisible: false,
      wickUpColor: upColor,
      wickDownColor: downColor,
    });

    // 副图：成交量柱状图
    const volumeSeries = chart.addSeries(HistogramSeries, {
      priceFormat: { type: "volume" },
      priceScaleId: "",
      priceLineVisible: false,
      lastValueVisible: false,
    });

    candleSeries.priceScale().applyOptions({
      scaleMargins: { top: 0.05, bottom: 0.28 },
    });

    volumeSeries.priceScale().applyOptions({
      scaleMargins: { top: 0.75, bottom: 0 },
    });

    chartRef.current = chart;
    candleSeriesRef.current = candleSeries;
    volumeSeriesRef.current = volumeSeries;

    markersPluginRef.current = createSeriesMarkers(candleSeries, []);

    const handleCrosshairMove = (param) => {
      if (
        !param ||
        !param.time ||
        !param.point ||
        param.point.x < 0 ||
        param.point.y < 0
      ) {
        setHoverInfo(null);
        return;
      }

      const candle = param.seriesData.get(candleSeries);
      if (!candle) {
        setHoverInfo(null);
        return;
      }

      const extra = dataMapRef.current[param.time] || {};

      setHoverInfo({
        time: param.time,
        open: candle.open,
        high: candle.high,
        low: candle.low,
        close: candle.close,
        vol: extra.vol,
        change: extra.change,
        amount: extra.amount,
        optType: extra.optType,
        stockName: extra.stockName,
        winChange: extra.winChange,
      });
    };

    chart.subscribeCrosshairMove(handleCrosshairMove);

    const handleResize = () => {
      if (containerRef.current) {
        chart.applyOptions({ width: containerRef.current.clientWidth });
        // 窗口变化后重新居中并留白
        const count = Object.keys(dataMapRef.current).length;
        if (count > 0) centerAllData(count);
      }
    };
    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
      chart.unsubscribeCrosshairMove(handleCrosshairMove);

      if (markersPluginRef.current) {
        markersPluginRef.current.detach();
        markersPluginRef.current = null;
      }

      chart.remove();
      chartRef.current = null;
      candleSeriesRef.current = null;
      volumeSeriesRef.current = null;
    };
  }, [height]);

  // 请求接口并渲染
  useEffect(() => {
    if (!candleSeriesRef.current || !volumeSeriesRef.current) return;

    const controller = new AbortController();

    const fetchDetail = async () => {
      try {
        setStatus("loading");
        setErrorMsg("");

        const response = await fetch(
          "/klineChart/list?stockCode=" + tscode + "&tradedate=" + tradedate,
          {
            method: "GET",
            signal: controller.signal,
          }
        );

        if (!response.ok) {
          throw new Error(`请求失败：HTTP ${response.status}`);
        }

        const result = await response.json();

        if (result.code !== "success") {
          throw new Error(result.desc || "接口返回失败");
        }

        const list = Array.isArray(result.data) ? result.data : [];

        if (list.length === 0) {
          candleSeriesRef.current.setData([]);
          volumeSeriesRef.current.setData([]);
          dataMapRef.current = {};
          markersPluginRef.current?.setMarkers([]);
          setStatus("empty");
          return;
        }

        // 把 "20261016" 转成 "2026-10-16"
        const formatTime = (t) => {
          const s = String(t ?? "");
          if (/^\d{8}$/.test(s)) {
            return `${s.slice(0, 4)}-${s.slice(4, 6)}-${s.slice(6, 8)}`;
          }
          return s;
        };

        const normalized = list
          .map((item) => ({
            time: formatTime(item.time),
            open: Number(item.open),
            high: Number(item.high),
            low: Number(item.low),
            close: Number(item.close),
            vol: Number(item.vol),
            change: item.change,
            amount: item.amount,
            optType: item.optType || "",
            stockName: item.stockName || "",
            winChange: Number(item.winChange),
          }))
          .filter(
            (item) =>
              item.time &&
              [item.open, item.high, item.low, item.close].every(
                (v) => !Number.isNaN(v)
              )
          )
          .sort((a, b) => new Date(a.time) - new Date(b.time));

        if (normalized.length === 0) {
          candleSeriesRef.current.setData([]);
          volumeSeriesRef.current.setData([]);
          dataMapRef.current = {};
          markersPluginRef.current?.setMarkers([]);
          setStatus("empty");
          return;
        }

        dataMapRef.current = Object.fromEntries(
          normalized.map((it) => [it.time, it])
        );

        candleSeriesRef.current.setData(
          normalized.map(({ time, open, high, low, close }) => ({
            time,
            open,
            high,
            low,
            close,
          }))
        );

        // 成交量柱状图（A 股：涨红跌绿）
        volumeSeriesRef.current.setData(
          normalized.map((it) => ({
            time: it.time,
            value: Number.isNaN(it.vol) ? 0 : it.vol,
            color: it.close >= it.open ? "#ef535080" : "#26a69a80",
          }))
        );

        // 标记所有 optType 非空的蜡烛：上下交替
        // 第 1 个：下方(belowBar) 向上箭头；第 2 个：上方(aboveBar) 向下箭头；循环交替
        let optIndex = 0;
        const optMarkers = normalized
          .filter((it) => it.optType && it.optType.trim() !== "")
          .map((it) => {
            const isBelow = optIndex % 2 === 0;
            optIndex += 1;
            return {
              time: it.time,
              position: isBelow ? "belowBar" : "aboveBar",
              color: "#f59e0b",
              shape: isBelow ? "arrowUp" : "arrowDown",
              text: it.optType,
              size: 1.5,
            };
          });

        if (markersPluginRef.current) {
          markersPluginRef.current.setMarkers(optMarkers);
        } else {
          markersPluginRef.current = createSeriesMarkers(
            candleSeriesRef.current,
            optMarkers
          );
        }

        // ↓↓↓ 全部数据居中显示，左侧少量留白，右侧更多留白
        centerAllData(normalized.length);

        setStatus("ready");
      } catch (err) {
        if (err.name === "AbortError") return;
        console.error("渲染蜡烛图失败：", err);
        setErrorMsg(err.message || "渲染蜡烛图失败");
        setStatus("error");
      }
    };

    fetchDetail();

    return () => controller.abort();
  }, [tscode, tradedate]);

  const isUp =
    hoverInfo && hoverInfo.close !== undefined && hoverInfo.open !== undefined
      ? hoverInfo.close >= hoverInfo.open
      : true;

  // optType 非空时显示原值
  const optTypeText =
    hoverInfo && hoverInfo.optType && String(hoverInfo.optType).trim() !== ""
      ? String(hoverInfo.optType)
      : "";

  const fmt = (v, digits = 2) =>
    v === undefined || v === null || Number.isNaN(Number(v))
      ? "-"
      : Number(v).toFixed(digits);

  const fmtVol = (v) => {
    if (v === undefined || v === null || Number.isNaN(Number(v))) return "-";
    const n = Number(v);
    if (n >= 100000000) return (n / 100000000).toFixed(2) + "亿";
    if (n >= 10000) return (n / 10000).toFixed(2) + "万";
    return String(n);
  };

  return (
    <div style={{ position: "relative", width: "100%" }}>
      {/* 悬停信息条 */}
      <div
        style={{
          position: "absolute",
          top: 8,
          left: 12,
          right: 60,
          zIndex: 10,
          display: "flex",
          flexWrap: "wrap",
          gap: "4px 14px",
          fontSize: 12,
          color: "#d1d4dc",
          background: "rgba(30,30,47,0.78)",
          padding: "4px 10px",
          borderRadius: 4,
          pointerEvents: "none",
          opacity: hoverInfo ? 1 : 0,
          transition: "opacity 0.15s",
        }}
      >
        {hoverInfo && (
          <>
            {hoverInfo.stockName && (
              <span style={{ color: "#0f0e0e", fontWeight: 600 }}>
                {hoverInfo.stockName}
              </span>
            )}
            <span>时间：{hoverInfo.time}</span>
            <span>开：{fmt(hoverInfo.open)}</span>
            <span style={{ color: isUp ? upColor : downColor }}>
              收：{fmt(hoverInfo.close)}
            </span>
            <span>高：{fmt(hoverInfo.high)}</span>
            <span>低：{fmt(hoverInfo.low)}</span>
            <span>
              量：
              <span style={{ color: isUp ? upColor : downColor }}>
                {fmtVol(hoverInfo.vol)}
              </span>
            </span>
            <span>
              涨跌：
              <span
                style={{
                  color:
                    hoverInfo.change === undefined
                      ? "#d1d4dc"
                      : Number(hoverInfo.change) >= 0
                      ? upColor
                      : downColor,
                }}
              >
                {hoverInfo.change !== undefined &&
                hoverInfo.change !== null
                  ? `${Number(hoverInfo.change) >= 0 ? "+" : ""}${fmt(
                      hoverInfo.change
                    )}%`
                  : "-"}
              </span>
            </span>
            <span>额：{fmtVol(hoverInfo.amount)}</span>
            <span>
              胜：
              <span
                style={{
                  color:
                    hoverInfo.winChange === undefined ||
                    Number.isNaN(Number(hoverInfo.winChange))
                      ? "#d1d4dc"
                      : Number(hoverInfo.winChange) >= 0
                      ? upColor
                      : downColor,
                }}
              >
                {hoverInfo.winChange !== undefined &&
                !Number.isNaN(Number(hoverInfo.winChange))
                  ? `${Number(hoverInfo.winChange) >= 0 ? "+" : ""}${fmt(
                      hoverInfo.winChange
                    )}%`
                  : "-"}
              </span>
            </span>

            {optTypeText && (
              <span style={{ color: "#f59e0b", fontWeight: 600 }}>
                ★ {optTypeText}
              </span>
            )}
          </>
        )}
      </div>

      <div
        ref={containerRef}
        style={{ width: "100%", borderRadius: 6, overflow: "hidden" }}
      />

      {status !== "ready" && (
        <div
          style={{
            position: "absolute",
            inset: 0,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: status === "error" ? "#ef5350" : "#d1d4dc",
            background: "rgba(30,30,47,0.55)",
            borderRadius: 6,
            fontSize: 13,
          }}
        >
          {status === "loading" && "正在加载蜡烛图..."}
          {status === "empty" && "暂无数据"}
          {status === "error" && `加载失败：${errorMsg}`}
        </div>
      )}
    </div>
  );
}

export default KLineChart;