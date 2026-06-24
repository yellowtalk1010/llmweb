import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";

function PushTushareStock() {
  const [searchParams] = useSearchParams();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [expandedModules, setExpandedModules] = useState({});

  /**
   * 下来菜单中选择项
   */
  const [modules, setModules] = useState([]);
  const [selectedModule, setSelectedModule] = useState("ALL_MODEL");

  const fetchData = async (modType = "") => {
    try {
      setLoading(true);
      const response = await fetch("/push_stocks/list?modType=" + modType);
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

  /***
   * 拉取模型类型数据
   */
  const fetchModules = async () => {
  try {
    const response = await fetch("/push_stocks/moduleList");

    if (!response.ok) {
      throw new Error("获取模块列表失败");
    }

    const result = await response.json();

    if (result.code === "success") {
      setModules(result.data || []);
    }
  } catch (e) {
    console.error(e);
  }
};

useEffect(() => {
  fetchModules();
}, []);

useEffect(() => {

  fetchData(selectedModule);

  const interval = setInterval(() => {
    fetchData(selectedModule);
  }, 10000);

  return () => clearInterval(interval);

}, [selectedModule]);

  const toggleModule = (index) => {
    setExpandedModules((prev) => ({ ...prev, [index]: !prev[index] }));
  };

 

  return (
    <div className="p-6 space-y-6">

      {loading && <p>Loading...</p>}
      {error && <p className="text-red-500">Error: {error}</p>}

      <div className="mb-4">
        <label className="mr-2 font-medium">
          模型：
        </label>

        <select
          value={selectedModule}
          onChange={(e) => setSelectedModule(e.target.value)}
          className="border rounded px-3 py-2"
        >
          {modules.map((item) => (
            
            <option
              key={item.cls}
              value={item.cls}
            >
              {item.name}
            </option>
          ))}
        </select>
      </div>

      {data.map((moduleItem, index) => (
        <div
            key={index}
            style={{
              border: "none",
              borderRadius: "16px",
              padding: "20px",
              boxShadow: "0 4px 6px rgba(0,0,0,0.1)",
              transition: "all 0.3s",
              backgroundColor: index % 2 === 0 ? "#dbeafe" : "#cdd1d3"  // 蓝色 / 灰色
            }}
        >


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
              <StockTable 
                list={moduleItem.heads} 
                refresh={() => fetchData(selectedModule)}
              />
            </div>
          )}

          {/* Histories */}
          {expandedModules[index] && moduleItem.histories && moduleItem.histories.length > 0 && (
            <div>
              <h3 className="font-medium mb-2">历史记录</h3>
              <StockTable 
                list={moduleItem.histories} 
                refresh={() => fetchData(selectedModule)}
              />
            </div>
          )}
        </div>
      ))}

    </div>
  );
}

function StockTable({ list, refresh  }) {

  /***
   * 删除关注
   */
  const delete_stock = async (tsCode, stockType) => {
    try {
      const response = await fetch(
        "/stocks/delete_stock?tsCode=" + tsCode + "&stockType=" + stockType,
        {
          method: "GET",
        }
      );

      if (!response.ok) {
        throw new Error("删除失败");
      }

      const result = await response.json();

      if (result.code === "success") {
        // alert(result.desc);
        // window.location.reload(); // 简单刷新
        if (refresh) {
          refresh();
        }
      }
    } catch (e) {
      console.error(e);
      alert("请求失败");
    }
  };

  /***
   * 添加关注
   */
  const add_stock = async (tsCode, stockType) => {
    try {
      const response = await fetch(
        "/stocks/add_stock?tsCode=" + tsCode + "&stockType=" + stockType,
        {
          method: "GET",
        }
      );

      if (!response.ok) {
        throw new Error("添加失败");
      }

      const result = await response.json();

      if (result.code === "success") {
        // alert(result.desc);
        // window.location.reload(); // 简单刷新
        if (refresh) {
          refresh();
        }
      }
    } catch (e) {
      console.error(e);
      alert("请求失败");
    }
  };

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm border-collapse">
        <thead>
        <tr className="border-b bg-gray-50">
          <th>序号</th>
          <th className="p-2 text-left">关注</th>
          <th className="p-2 text-left">资金</th>
          <th className="p-2 text-left">名称</th>
          <th className="p-2 text-left">代码</th>
          <th className="p-2 text-left">备注</th>
          <th className="p-2 text-left">行业</th>
          <th className="p-2 text-left">地区</th>
          <th className="p-2 text-left">胜率</th>
          <th className="p-2 text-left">活跃率</th>
          <th className="p-2 text-left">涨停</th>
          <th className="p-2 text-left">跌停</th>
          <th className="p-2 text-left">风险</th>
          <th className="p-2 text-left">来源</th>
        </tr>
        </thead>
        <tbody>
        {list.map((item, idx) => (
            <tr key={idx} className="border-b hover:bg-gray-50">

              <td>{idx + 1}</td>
              <td className="p-2">
                {
                      item.attention?(
                        <span>
                          {item.attention}
                          <button
                            onClick={() => delete_stock(item.ts_code, "attention")}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >移除</button>
                        </span>
                      )
                      :(
                        <span>
                          <button
                            onClick={() => add_stock(item.ts_code, "attention")}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >关注</button>

                        </span>
                      )
                    }
              </td>
            
              <td className="p-2">
                  <a
                    href={`/pages/Moneyflow?tsCode=${item.ts_code}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-600 underline cursor-pointer"
                  >
                    资金
                  </a>
              </td>  

             
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
              <td className="p-2">{item.remark}</td>
              <td className="p-2">{item.industry}</td>
              <td className="p-2">{item.area}</td>
              <td className="p-2 text-green-600">{item.modWinRate}</td>
              <td className="p-2">{item.turnoverRate}</td>
              <td className="p-2 text-red-500">{item.limitUp}</td>
              <td className="p-2 text-blue-500">{item.limitDown}</td>
              <td className="p-2">{item.upperShadow}</td>
              <td className="p-2">{item.fileName}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default PushTushareStock;