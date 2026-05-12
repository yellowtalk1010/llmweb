import { useSearchParams } from "react-router-dom";
import { useState, useEffect, useCallback } from "react";

function AllStocks() {

  const [searchParams] = useSearchParams();

  const [allStocksData, setAllStocksData] = useState([]); 

  const fetchMoneyFlow = async () => {

    try { 

      const response = await fetch(
        "/stocks/all?desc="
      );

      const result = await response.json();

      console.info(result)

      if (result.code === "success") {
        setAllStocksData(
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

  return (
    <div className="p-6">
 
      {(
        <div className="overflow-x-auto rounded-xl shadow">
          <table className="w-full text-sm border-collapse bg-white">

            <thead>
              <tr className="bg-gray-100 border-b">
                <th className="p-3 text-left">代码</th>
                <th className="p-3 text-left">代码名称</th>
              </tr>
            </thead>

            <tbody>
              {allStocksData.map((item, index) => (
                <tr
                  key={index}
                  className="border-b hover:bg-gray-50"
                >

                  <td className="p-3">
                    {item.ts_code}
                  </td>

                  <td className="p-3">
                    {item.name}
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

export default AllStocks;