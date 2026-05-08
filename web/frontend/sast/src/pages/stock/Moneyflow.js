import { useSearchParams } from "react-router-dom";
import { useState, useEffect, useCallback } from "react";

function Moneyflow() {

  const [searchParams] = useSearchParams();

  const [moneyFlowData, setMoneyFlowData] = useState([]); 

  // 获取上个页面传入的 tsCode
  const tsCode = searchParams.get("tsCode");
  console.info("halo....")

  /**
   * 获取资金流向
   */
  const fetchMoneyFlow = async () => {
    if (!tsCode) {
      console.info("缺少 tsCode 参数");
      return;
    }

    try { 

      const response = await fetch(
        "/money_flow/getTsCode?tsCode=" + tsCode
      );

      const result = await response.json();

      console.info(result)

      if (result.code === "success") {
        setMoneyFlowData(
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
  }, [tsCode]);

  return (
    <div className="p-6">
 
      {(
        <div className="overflow-x-auto rounded-xl shadow">
          <table className="w-full text-sm border-collapse bg-white">

            <thead>
              <tr className="bg-gray-100 border-b">
                <th className="p-3 text-left">日期</th>
                <th className="p-3 text-left">代码</th>
                <th className="p-3 text-left">代码名称</th>

                <th className="p-3 text-left">主力净流入</th>
                <th className="p-3 text-left">超大单净流入</th>
                <th className="p-3 text-left">大单净流入</th>
                <th className="p-3 text-left">中单净流入</th>
                <th className="p-3 text-left">小单净流入</th>

              </tr>
            </thead>

            <tbody>
              {moneyFlowData.map((item, index) => (
                <tr
                  key={index}
                  className="border-b hover:bg-gray-50"
                >

                  <td className="p-3">
                    {item.trade_date}
                  </td>

                  <td className="p-3">
                    {item.ts_code}
                  </td>

                  <td className="p-3">
                    {item.name}
                  </td>

                  <td className="p-3">
                    {item.net_amount}【{item.net_amount_rate}%】
                  </td>

                  <td className="p-3">
                    {item.buy_elg_amount}【{item.buy_elg_amount_rate}%】
                  </td>

                  <td className="p-3">
                    {item.buy_lg_amount}【{item.buy_lg_amount_rate}%】
                  </td>

                  <td className="p-3">
                    {item.buy_md_amount}【{item.buy_md_amount_rate}%】
                  </td>

                  <td className="p-3 text-red-500 font-medium">
                    {item.buy_sm_amount}【{item.buy_sm_amount_rate}%】
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

export default Moneyflow;