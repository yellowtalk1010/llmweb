import { useSearchParams } from "react-router-dom";
import { useState, useEffect, useCallback } from "react";

function BuyAttention() {

  const [searchParams] = useSearchParams();

  const [buyAttentionData, setBuyAttentionData] = useState([]); 

 

  /**
   * 获取资金流向
   */
  const fetchMoneyFlow = async () => {

    try { 
      const response = await fetch(
        "/stocks/my"
      );

      const result = await response.json();

      console.info(result)

      if (result.code === "success") {
        setBuyAttentionData(
            Array.isArray(result.data)
                ? result.data
                : []
            );
      }

    } catch (e) {

    } finally {
 
    }
  };

  useEffect(() => {
    fetchMoneyFlow();
  }, []);


  /***
   * 删除购买
   */
  const deleteBuy = async (tsCode) => {
    try {
      const response = await fetch(
        "/stocks/delete_buy?tsCode=" + tsCode,
        {
          method: "GET",
        }
      );

      if (!response.ok) {
        throw new Error("添加关注失败");
      }

      const result = await response.json();

      if (result.code === "success") {
        alert(result.desc);
        window.location.reload(); // 简单刷新
      }
    } catch (e) {
      console.error(e);
      alert("请求失败");
    }
  };

  /***
   * 删除关注
   */
  const deleteAttention = async (tsCode) => {
    try {
      const response = await fetch(
        "/stocks/delete_attention?tsCode=" + tsCode,
        {
          method: "GET",
        }
      );

      if (!response.ok) {
        throw new Error("添加关注失败");
      }

      const result = await response.json();

      if (result.code === "success") {
        alert(result.desc);
        window.location.reload(); // 简单刷新
      }
    } catch (e) {
      console.error(e);
      alert("请求失败");
    }
  };

  return (
    <div className="p-6">
 
      {(
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
              {buyAttentionData.map((item, index) => (
                <tr
                  key={index}
                  className="border-b hover:bg-gray-50"
                >
                  <td className="p-3">
                    {
                      item.buy?(
                        <span>
                          {item.buy}
                          <button
                            onClick={() => deleteBuy(item.ts_code)}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >移除</button>
                        </span>
                      ) : (
                        <span>

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
                            onClick={() => deleteAttention(item.ts_code)}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                          >移除</button>
                        </span>
                      )
                      :(
                        <span></span>
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

                  <td className="p-3">
                    {item.ts_code}
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

                </tr>
              ))}
            </tbody>

          </table>
        </div>
      )}

    </div>
  );
}

export default BuyAttention;