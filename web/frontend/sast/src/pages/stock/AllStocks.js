import { useSearchParams } from "react-router-dom";
import { useState, useEffect } from "react";

function AllStocks() {

  const [searchParams] = useSearchParams();

  const [allStocksData, setAllStocksData] = useState([]);

  // 输入框内容
  const [inputKeyword, setInputKeyword] = useState("");

  // 请求后台数据
  const fetchMoneyFlow = async (desc = "") => {

    try {

      const response = await fetch(
        `/stocks/all?desc=${encodeURIComponent(desc)}`
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

      </div>

      <div className="overflow-x-auto rounded-xl shadow">
        <table className="w-full text-sm border-collapse bg-white">

          <thead>
            <tr className="bg-gray-100 border-b">
              <th className="p-3 text-left">购买</th>
              <th className="p-3 text-left">关注</th>
              <th className="p-3 text-left">资金</th>
              <th className="p-3 text-left">代码</th>
              <th className="p-3 text-left">代码名称</th>
            </tr>
          </thead>

          <tbody>

            {allStocksData.length > 0 ? (
              allStocksData.map((item, index) => (
                <tr
                  key={index}
                  className="border-b hover:bg-gray-50"
                >

                  <td className="p-3">
                    
                  </td>
                  <td className="p-3">
                    
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

                  <td className="p-3">
                    {item.ts_code}
                  </td>

                  <td className="p-3">
                    {item.name}
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