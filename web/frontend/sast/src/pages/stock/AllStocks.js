import { useSearchParams } from "react-router-dom";
import { useState, useEffect } from "react";

function AllStocks() {

  const [searchParams] = useSearchParams();

  const [allStocksData, setAllStocksData] = useState([]);
  const [status, setStatus] = useState("ma4");

  // 输入框内容
  const [inputKeyword, setInputKeyword] = useState("");

  // 请求后台数据
  const fetchMoneyFlow = async (desc = "") => {

    try {

      const response = await fetch(
        `/stocks/all?desc=${encodeURIComponent(desc)}&status=${status}`
      );

      const result = await response.json();

      console.info(result);

      if (result.code === "success") {

        setAllStocksData(
          Array.isArray(result.data)
            ? result.data
            : []
        );

      }

    } catch (e) {
      console.error(e);
    }
  };

  // 页面初始化加载
  useEffect(() => {
    fetchMoneyFlow();
  }, []);

  // 点击检索按钮
  const handleSearch = () => {
    fetchMoneyFlow(inputKeyword);
  };


  /***
   * 删除购买
   */
  const deleteStock = async (tsCode, stockType) => {
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
        handleSearch()
      }
    } catch (e) {
      console.error(e);
      alert("请求失败");
    }
  };

  const addStock = async (tsCode, stockType) => {
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
        handleSearch()
      }
    } catch (e) {
      console.error(e);
      alert("请求失败");
    }
  };

  return (
    <div className="p-6">

      {/* 检索区域 */}
      <div className="mb-4 flex gap-2">

        <input
          type="text"
          placeholder="输入股票代码或名称检索"
          value={inputKeyword}
          onChange={(e) => setInputKeyword(e.target.value)}
          className="w-full md:w-80 px-4 py-2 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
        />

        <button
          onClick={handleSearch}
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
        >
          检索
        </button>

        <div className="flex items-center gap-4">

          <label className="flex items-center gap-1 cursor-pointer">
            <input
              type="radio"
              name="status"
              value="ma4"
              checked={status === "ma4"}
              onChange={(e) => setStatus(e.target.value)}
            />
            ma4
          </label>

           <label className="flex items-center gap-1 cursor-pointer">
            <input
              type="radio"
              name="status"
              value="ma5"
              checked={status === "ma5"}
              onChange={(e) => setStatus(e.target.value)}
            />
            ma5
          </label>

          <label className="flex items-center gap-1 cursor-pointer">
            <input
              type="radio"
              name="status"
              value="my"
              checked={status === "my"}
              onChange={(e) => setStatus(e.target.value)}
            />
            我的
          </label>

          <label className="flex items-center gap-1 cursor-pointer">
            <input
              type="radio"
              name="status"
              value="all"
              checked={status === "all"}
              onChange={(e) => setStatus(e.target.value)}
            />
            全部
          </label>



        </div>

      </div>

      <div className="overflow-x-auto rounded-xl shadow">
        <table className="w-full text-sm border-collapse bg-white">

          <thead>
            <tr className="bg-gray-100 border-b">
              <th className="p-3 text-left">序号</th>
              <th className="p-3 text-left">购买</th>
              <th className="p-3 text-left">关注</th>
              <th className="p-3 text-left">资金</th>
              <th className="p-3 text-left">代码</th>
              <th className="p-3 text-left">代码名称</th>
              <th className="p-3 text-left">概念</th>
              <th className="p-3 text-left">备注</th>
            </tr>
          </thead>

          <tbody>

            {allStocksData.length > 0 ? (
              allStocksData.map((item, index) => (
                <tr
                  key={index}
                  className="border-b hover:bg-gray-50"
                >
                  <td>{index+1}</td>    
                  <td className="p-3">
                    {
                      item.buy?(
                        <span>
                          {item.buy}
                          <button
                            onClick={() => deleteStock(item.stockCode, "buy")}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >移除</button>
                        </span>
                      ) : (
                        <span>
                          <button
                            onClick={() => addStock(item.stockCode, "buy")}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >购买</button>
                        </span>
                      )
                    }
                  </td>
                  <td className="p-3">
                    {
                      item.attention?(
                        <span>
                          {item.attention}
                          <button
                            onClick={() => deleteStock(item.stockCode, "attention")}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >移除</button>
                        </span>
                      )
                      :(
                        <span>
                          <button
                            onClick={() => addStock(item.stockCode, "attention")}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >关注</button>

                        </span>
                      )
                    }
                  </td>

                  <td className="p-2">
                      <a
                        href={`/pages/Moneyflow?tsCode=${item.stockCode}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-blue-600 underline cursor-pointer"
                      >
                        资金
                      </a>
                  </td>  

                  <td className="p-3">
                    {item.stockCode}
                  </td>


                  <td className="p-3">
                    <a
                      href={item.eastmoneyURL}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 underline"
                    >
                      {item.name}
                    </a>
                    
                  </td>

                  
                  <td className="p-2">
                      <a
                        href={item.conceptURL}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-blue-600 underline cursor-pointer"
                      >
                        概念
                      </a>
                  </td>  
                  
                  <td>
                    {item.remark}
                    <br/>
                  </td>

                </tr>
              ))
            ) : (
              <tr>
                <td
                  colSpan="2"
                  className="p-6 text-center text-gray-500"
                >
                  未找到匹配数据
                </td>
              </tr>
            )}

          </tbody>

        </table>
      </div>

    </div>
  );
}

export default AllStocks;