import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";

function PushTushareStock() {
  const [searchParams] = useSearchParams();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [expandedModules, setExpandedModules] = useState({});

  const fetchData = async () => {
    try {
      setLoading(true);
      const response = await fetch("/push_stocks/list");
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      const result = await response.json();
      if (result.code === "success") {
        setData(result.data);
      }
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 10000);
    return () => clearInterval(interval);
  }, []);

  const toggleModule = (index) => {
    setExpandedModules((prev) => ({ ...prev, [index]: !prev[index] }));
  };

  return (
    <div className="p-6 space-y-6">

      {loading && <p>Loading...</p>}
      {error && <p className="text-red-500">Error: {error}</p>}

      {data.map((moduleItem, index) => (
        <div
  key={index}
  className={`
    rounded-2xl p-5 shadow-md transition-all duration-300
    ${index % 2 === 0 
      ? "bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100" 
      : "bg-gradient-to-r from-gray-50 to-slate-100 border border-gray-200"}
    hover:shadow-xl hover:scale-[1.01]
  `}
>

        <hr style={{
          border: "none",
          borderTop: "2px solid #6207f4",
          width: "100%"
        }}></hr>

          <div className="flex justify-between items-center">
            
            <h2>
              <span style={{ color: "red" }}>
                  {moduleItem.time}
                </span>
            </h2>
            <h4 className="text-lg font-semibold mb-3">
              {moduleItem.module}
            </h4>
            {moduleItem.histories && moduleItem.histories.length > 0 && (
              <button
                onClick={() => toggleModule(index)}
                className="text-blue-600 underline"
              >
                {expandedModules[index] ? "收起历史" : "展开历史"}
              </button>
            )}
          </div>

          {/* Heads */}
          {moduleItem.heads && moduleItem.heads.length > 0 && (
            <div className="mb-4">
              <h3 className="font-medium mb-2">最新信号</h3>
              <StockTable list={moduleItem.heads} />
            </div>
          )}

          {/* Histories */}
          {expandedModules[index] && moduleItem.histories && moduleItem.histories.length > 0 && (
            <div>
              <h3 className="font-medium mb-2">历史记录</h3>
              <StockTable list={moduleItem.histories} />
            </div>
          )}
        </div>
      ))}

    </div>
  );
}

function StockTable({ list }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm border-collapse">
        <thead>
        <tr className="border-b bg-gray-50">
          <th className="p-2 text-left">名称</th>
          <th className="p-2 text-left">代码</th>
          <th className="p-2 text-left">行业</th>
          <th className="p-2 text-left">地区</th>
          <th className="p-2 text-left">胜率</th>
          <th className="p-2 text-left">换手率</th>
          <th className="p-2 text-left">涨停</th>
          <th className="p-2 text-left">跌停</th>
          <th className="p-2 text-left">来源</th>
        </tr>
        </thead>
        <tbody>
        {list.map((item, idx) => (
            <tr key={idx} className="border-b hover:bg-gray-50">
              <td className="p-2 font-medium">
                <a
                  href={item.eastmoneyURL}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 underline"
                >
                {item.name} 
                </a>
              </td>
              <td className="p-2">{item.ts_code}</td>
              <td className="p-2">{item.industry}</td>
              <td className="p-2">{item.area}</td>
              <td className="p-2 text-green-600">{item.modWinRate}</td>
              <td className="p-2">{item.turnoverRate}</td>
              <td className="p-2 text-red-500">{item.limitUp}</td>
              <td className="p-2 text-blue-500">{item.limitDown}</td>
              
              <td className="p-2">{item.fileName}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default PushTushareStock;