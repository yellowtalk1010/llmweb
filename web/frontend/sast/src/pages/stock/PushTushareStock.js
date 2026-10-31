import './../../index.css';
import { useState, useEffect, Fragment } from "react";
import { useSearchParams } from "react-router-dom";
import KLineChart from "./KLineChart";

/***
 * 模型推荐股票页
 */
function PushTushareStock() {
  const [searchParams] = useSearchParams();
  const [data, setData] = useState([]);
  const [hotStock, setHotStock] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [expandedModules, setExpandedModules] = useState({});

  // 是否集中计算相似
  const [group, setGroup] = useState(false);

  /**
   * 下来菜单中选择项
   */
  const [modules, setModules] = useState([]);
  const [selectedModule, setSelectedModule] = useState("ALL_MODEL"); // ALL_MODEL
  const [tradedate, setTradedate] = useState("");

  // 是否自动刷新
  const [autoRefresh, setAutoRefresh] = useState(false);

  const fetchData = async (modType = "") => {
    try {
      setLoading(true);
      const response = await fetch(
        "/push_stocks/list?modType=" + modType +
        "&tradedate=" + tradedate +
        "&group=" + group
      );
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      const result = await response.json();
      if (result.code === "success") {
        setGroup(false);
        setData(result.data);
        setHotStock(Array.isArray(result.hotStock) ? result.hotStock : []);
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

    if (!autoRefresh) {
      return;
    }

    const interval = setInterval(() => {
      fetchData(selectedModule);
    }, 1000 * 30); //刷新频率

    return () => clearInterval(interval);
  }, [autoRefresh]);

  const toggleModule = (index) => {
    setExpandedModules((prev) => ({ ...prev, [index]: !prev[index] }));
  };

  return (
    <div className="p-6 space-y-6">

      <div className="mb-4">
        <label className="mr-2 font-medium">
          交易日：
        </label>
        <input
          type="date"
          value={tradedate}
          onChange={(e) => {
            setGroup(false);
            setTradedate(e.target.value);
          }}
          className="border rounded px-3 py-2"
        />

        <label className="mr-2 font-medium">
          模型：
        </label>

        <select
          value={selectedModule}
          onChange={(e) => {
            setGroup(false);
            setSelectedModule(e.target.value);
          }}
          className="border rounded px-3 py-2"
        >
          {modules.map((item) => (
            <option key={item.cls} value={item.cls}>
              {item.name}
            </option>
          ))}
        </select>

        <label className="  items-center gap-1">
          <input
            type="checkbox"
            checked={group}
            onChange={(e) => setGroup(e.target.checked)}
          />
          集算
        </label>

        <button
          onClick={() => fetchData(selectedModule)}
          className="ml-2 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          查询  {loading && <p>Loading...</p>}
        </button>

        <label className="ml-2 items-center gap-1">
          <input
            type="checkbox"
            checked={autoRefresh}
            onChange={(e) => setAutoRefresh(e.target.checked)}
          />
          刷新
        </label>

        <label>
          {/* {loading && <p>Loading...</p>} */}
          {error && <p className="text-red-500">Error: {error}</p>}
        </label>

      </div>
      <div>
        <label>
          {hotStock.map((item, index) => (
            <div key={index}>
              <span>
                {item}
              </span>
            </div>
          ))}
        </label>
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

          <div className="flex flex-col">

            <h4 className="text-lg font-semibold mb-3">
              {moduleItem.module}
            </h4>
            <br></br>
            <h2>
              <span style={{ color: "red" }}>
                {moduleItem.time}
              </span>
            </h2>

            <br></br>
            {moduleItem.histories && moduleItem.histories.length > 0 && (
              <button
                onClick={() => toggleModule(index)}
                className="self-start px-3 py-1 text-xs text-white bg-blue-600 rounded hover:bg-blue-400"
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

function StockTable({ list, refresh }) {
  // 记录哪些行已展开：{ [rowKey]: true }
  const [expandedRows, setExpandedRows] = useState({});

  /***
   * 展开 / 收起某一行
   */
  const toggleRow = (rowKey) => {
    setExpandedRows((prev) => {
      const next = { ...prev };
      if (next[rowKey]) {
        delete next[rowKey];
      } else {
        next[rowKey] = true;
      }
      return next;
    });
  };

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
      <table className="w-full text-sm border-collapse bg-white">
        <thead>
          <tr className="border-b bg-gray-100">
            <th className="p-2 text-left border border-gray-200">序号</th>
            <th className="p-2 text-left border border-gray-200">关注</th>
            <th className="p-2 text-left border border-gray-200">备注</th>
            <th className="p-2 text-left border border-gray-200">代码/名称</th>
            <th className="p-2 text-left border border-gray-200">龙虎榜</th>
            <th className="p-2 text-left border border-gray-200">相似率</th>
            <th className="p-2 text-left border border-gray-200">活跃率</th>
            <th className="p-2 text-left border border-gray-200">风险</th>
            <th className="p-2 text-left border border-gray-200">来源</th>
          </tr>
        </thead>
        <tbody>
          {list.map((item, idx) => {
            const rowKey = `${item.ts_code}_${item.tradeDate}_${idx}`;
            const expanded = !!expandedRows[rowKey];

            return (
              <Fragment key={rowKey}>
                <tr className="border-b group">
                  <td
                    className="cursor-pointer select-none p-2"
                    onClick={() => toggleRow(rowKey)}
                    title={expanded ? "收起蜡烛图" : "展开蜡烛图"}
                  >
                    <span className="inline-block w-4 text-center mr-1">
                      {expanded ? "▼" : "▶"}
                    </span>
                    {idx + 1}
                    <br />
                    {item.tradeDate}
                    <br />
                    {item.modClsName}
                    <br />
                    {item.modWinRate}
                  </td>

                  <td className="p-2">
                    {
                      item.attention ? (
                        <span>
                          {item.attention}
                          <button
                            onClick={() => delete_stock(item.ts_code, "attention")}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >移除</button>
                        </span>
                      )
                        : (
                          <span>
                            <button
                              onClick={() => add_stock(item.ts_code, "attention")}
                              className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                            >关注</button>
                          </span>
                        )
                    }
                  </td>

                  <td className="p-2" style={{ minWidth: "500px" }}>
                    <a
                      href={item.zjlxURL}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 underline"
                    >
                      资金
                    </a>
                    &nbsp;&nbsp;
                    <a
                      href={item.conceptURL}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 underline"
                    >
                      概念
                    </a>
                    &nbsp;&nbsp;
                    {item.remark}
                    <hr />
                    <span style={{ color: "red", fontWeight: "bold" }}>
                      热点：{item.hotSectors}
                    </span>
                    <hr />
                    <span>
                      冰点：{item.coolSectors}
                    </span>
                  </td>
                  <td className="p-3 max-w-[200px] break-words">
                    {item.ts_code}
                    <br />
                    <br />
                    <a
                      href={item.eastmoneyURL}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 underline"
                    >
                      {item.name}
                    </a>
                  </td>
                  <td>
                    <a
                      href={`/pages/TopInst?ts_code=${item.ts_code}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 underline"
                    >
                      {item.topInstitutions}
                    </a>
                  </td>
                  <td className="p-2">
                    <a
                      href={`/pages/Similarity?tsCode=${item.ts_code}&tradeDate=${item.tradeDate}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 underline"
                    >
                      <span>{item.similarity}</span>
                    </a>
                  </td>
                  <td className="p-2">
                    {item.turnoverRate}
                    <br />
                    {item.limitUp}
                    <br />
                    {item.limitDown}
                  </td>
                  <td className="p-2">{item.upperShadow}</td>
                  <td className="p-2">{item.fileName}</td>
                </tr>

                {expanded && (
                  <tr>
                    <td
                      colSpan={9}
                      className="p-2"
                      style={{ background: "#12121c" }}
                    >
                      <KLineChart
                        tscode={item.ts_code}
                        tradedate={item.tradeDate}
                      />
                    </td>
                  </tr>
                )}
              </Fragment>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

export default PushTushareStock;