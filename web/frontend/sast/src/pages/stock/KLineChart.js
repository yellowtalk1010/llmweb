import { useEffect, useRef, useState } from "react";
import {
  createChart,
  CrosshairMode,
  CandlestickSeries,
} from "lightweight-charts";

/***
 * 股票蜡烛图组件
 * 暂时使用 10 组默认数据，后续替换为 /stock/detail 接口请求
 */
function KLineChart({ tscode, tradedate, height = 360 }) {
  const containerRef = useRef(null);
  const chartRef = useRef(null);
  const seriesRef = useRef(null);

  const [status, setStatus] = useState("loading");
  const [errorMsg, setErrorMsg] = useState("");

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
    });

    // v5 写法：addSeries(CandlestickSeries, options)
    const series = chart.addSeries(CandlestickSeries, {
      upColor: "#26a69a",
      downColor: "#ef5350",
      borderVisible: false,
      wickUpColor: "#26a69a",
      wickDownColor: "#ef5350",
    });

    chartRef.current = chart;
    seriesRef.current = series;

    const handleResize = () => {
      if (containerRef.current) {
        chart.applyOptions({ width: containerRef.current.clientWidth });
      }
    };
    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
      chart.remove();
      chartRef.current = null;
      seriesRef.current = null;
    };
  }, [height]);

  // 渲染数据（暂时使用 10 组默认数据）
  useEffect(() => {
    if (!seriesRef.current) return;

    const fetchDetail = async () => {
      try {
        setStatus("loading");
        setErrorMsg("");

        // ===== 默认数据（后续替换为接口请求）=====
        // 接口预期格式：
        // { time: '2024-01-02', open: 100, high: 105, low: 99, close: 104, vol: 9999, change: 1.0, amount: 30000 }
        const mockList = [
          { time: "2024-01-02", open: 100.0, high: 105.2, low: 98.8, close: 104.1, vol: 9999, change: 1.0, amount: 30000 },
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

        // 模拟异步请求的短暂延迟，便于看到 loading 状态
        await new Promise((resolve) => setTimeout(resolve, 200));

        const list = mockList;

        if (!Array.isArray(list) || list.length === 0) {
          seriesRef.current.setData([]);
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
          seriesRef.current.setData([]);
          setStatus("empty");
          return;
        }

        seriesRef.current.setData(normalized);
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

  return (
    <div style={{ position: "relative", width: "100%" }}>
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