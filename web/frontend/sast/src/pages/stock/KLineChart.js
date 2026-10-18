import { useEffect, useRef, useState } from "react";
import {
  createChart,
  CrosshairMode,
  CandlestickSeries,
  HistogramSeries,
} from "lightweight-charts";

/***
 * 股票蜡烛图组件
 * 暂时使用 10 组默认数据，后续替换为 /stock/detail 接口请求
 * 主图：蜡烛图；副图：成交量柱状图；悬停显示 OHLC + vol + change + amount
 */
function KLineChart({ tscode, tradedate, height = 360 }) {
  const containerRef = useRef(null);
  const chartRef = useRef(null);
  const candleSeriesRef = useRef(null);
  const volumeSeriesRef = useRef(null);

  // 保存完整原始数据：{ [time]: { open, high, low, close, vol, change, amount } }
  const dataMapRef = useRef({});

  const [status, setStatus] = useState("loading");
  const [errorMsg, setErrorMsg] = useState("");
  const [hoverInfo, setHoverInfo] = useState(null);

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
      },
      // 右侧价格轴：主图占上方 75%，成交量占下方 25%
      rightPriceScale: {
        borderColor: "#485c7b",
      },
    });

    // 主图：蜡烛图
    const candleSeries = chart.addSeries(CandlestickSeries, {
      upColor: "#26a69a",
      downColor: "#ef5350",
      borderVisible: false,
      wickUpColor: "#26a69a",
      wickDownColor: "#ef5350",
    });

    // 副图：成交量柱状图（独立价格刻度，挂在图表下方）
    const volumeSeries = chart.addSeries(HistogramSeries, {
      priceFormat: { type: "volume" },
      priceScaleId: "", // 独立刻度，不与主图共享
      priceLineVisible: false,
      lastValueVisible: false,
    });

    // 主图占上方 75%
    candleSeries.priceScale().applyOptions({
      scaleMargins: {
        top: 0.05,
        bottom: 0.28,
      },
    });

    // 成交量占下方 25%
    volumeSeries.priceScale().applyOptions({
      scaleMargins: {
        top: 0.75,
        bottom: 0,
      },
    });

    chartRef.current = chart;
    candleSeriesRef.current = candleSeries;
    volumeSeriesRef.current = volumeSeries;

    // ===== 悬停监听 =====
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
      });
    };

    chart.subscribeCrosshairMove(handleCrosshairMove);

    const handleResize = () => {
      if (containerRef.current) {
        chart.applyOptions({ width: containerRef.current.clientWidth });
      }
    };
    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
      chart.unsubscribeCrosshairMove(handleCrosshairMove);
      chart.remove();
      chartRef.current = null;
      candleSeriesRef.current = null;
      volumeSeriesRef.current = null;
    };
  }, [height]);

  // 渲染数据（暂时使用 10 组默认数据）
  useEffect(() => {
    if (!candleSeriesRef.current || !volumeSeriesRef.current) return;

    const fetchDetail = async () => {
      try {
        setStatus("loading");
        setErrorMsg("");

        // ===== 默认数据（后续替换为接口请求）=====
        const mockList = [
          { time: "2023-12-30", open: 100.0, high: 105.2, low: 98.8, close: 104.1, vol: 9999, change: 1.0, amount: 30000 },
          { time: "2024-01-03", open: 104.1, high: 107.5, low: 102.3, close: 103.0, vol: 11200, change: -1.1, amount: 33000 },
          { time: "2024-01-04", open: 103.0, high: 106.8, low: 101.5, close: 105.6, vol: 12800, change: 2.5, amount: 35500 },
          { time: "2024-01-05", open: 105.6, high: 109.4, low: 104.2, close: 108.3, vol: 14300, change: 2.6, amount: 38200 },
          { time: "2024-01-08", open: 108.3, high: 110.1, low: 105.7, close: 106.4, vol: 9800, change: -1.8, amount: 31200 },
          { time: "2024-01-09", open: 106.4, high: 108.9, low: 103.8, close: 104.2, vol: 10500, change: -2.1, amount: 29800 },
          { time: "2024-01-10", open: 104.2, high: 107.6, low: 102.9, close: 107.1, vol: 11900, change: 2.8, amount: 34100 },
          { time: "2024-01-11", open: 107.1, high: 111.3, low: 106.0, close: 110.5, vol: 15600, change: 3.2, amount: 40100 },
          { time: "2024-01-12", open: 110.5, high: 112.8, low: 108.4, close: 109.2, vol: 13200, change: -1.2, amount: 36800 },
          { time: "2024-01-15", open: 109.2, high: 110.6, low: 106.5, close: 107.8, vol: 10100, change: -1.3, amount: 32500 },
        ];

        await new Promise((resolve) => setTimeout(resolve, 200));

        const list = mockList;

        if (!Array.isArray(list) || list.length === 0) {
          candleSeriesRef.current.setData([]);
          volumeSeriesRef.current.setData([]);
          dataMapRef.current = {};
          setStatus("empty");
          return;
        }

        const normalized = list
          .map((item) => ({
            time: item.time,
            open: Number(item.open),
            high: Number(item.high),
            low: Number(item.low),
            close: Number(item.close),
            vol: Number(item.vol),
            change: item.change,
            amount: item.amount,
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
          setStatus("empty");
          return;
        }

        // 保存完整数据映射
        dataMapRef.current = Object.fromEntries(
          normalized.map((it) => [it.time, it])
        );

        // 蜡烛图只接收 OHLC
        candleSeriesRef.current.setData(
          normalized.map(({ time, open, high, low, close }) => ({
            time,
            open,
            high,
            low,
            close,
          }))
        );

        // 成交量柱状图：涨绿跌红
        volumeSeriesRef.current.setData(
          normalized.map((it) => ({
            time: it.time,
            value: Number.isNaN(it.vol) ? 0 : it.vol,
            color: it.close >= it.open ? "#26a69a80" : "#ef535080",
          }))
        );

        chartRef.current.timeScale().fitContent();
        setStatus("ready");
      } catch (err) {
        console.error("渲染蜡烛图失败：", err);
        setErrorMsg(err.message || "渲染蜡烛图失败");
        setStatus("error");
      }
    };

    fetchDetail();
  }, [tscode, tradedate]);

  const isUp =
    hoverInfo && hoverInfo.close !== undefined && hoverInfo.open !== undefined
      ? hoverInfo.close >= hoverInfo.open
      : true;

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
            <span>时间：{hoverInfo.time}</span>
            <span>开：{fmt(hoverInfo.open)}</span>
            <span style={{ color: isUp ? "#26a69a" : "#ef5350" }}>
              收：{fmt(hoverInfo.close)}
            </span>
            <span>高：{fmt(hoverInfo.high)}</span>
            <span>低：{fmt(hoverInfo.low)}</span>
            <span>
              量：
              <span style={{ color: isUp ? "#26a69a" : "#ef5350" }}>
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
                      ? "#26a69a"
                      : "#ef5350",
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