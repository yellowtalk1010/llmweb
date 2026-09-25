import { useSearchParams } from "react-router-dom";
import { useState, useEffect } from "react";

function Similarity() {

  const [searchParams] = useSearchParams();

  const tsCode = searchParams.get("tsCode");
  const tradeDate = searchParams.get("tradeDate");

  const [loading, setLoading] = useState(true);

  const [similartyDto, setSimilartyDto] = useState({
    stockCode: "",
    stockName: "",
    tradeDate: "",
    sampleNumber: 0,
    winate: 0,
    desc: "",
    sampleList: []
  });

  const fetchSimilarty = async () => {

    if (!tsCode) {
      return;
    }

    try {

      setLoading(true);

      const response = await fetch(
        "/stock_similar/getTsCode?tsCode=" +
        tsCode +
        "&tradeDate=" +
        tradeDate
      );

      const result = await response.json();

      if (result.code === "success") {
        setSimilartyDto(result.data);
      }

    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSimilarty();
  }, [tsCode, tradeDate]);

  const successCount =
    Math.round(
      similartyDto.sampleNumber * similartyDto.winate
    );

  const failCount =
    similartyDto.sampleNumber - successCount;

  return (
    <div className="p-6 bg-slate-100 min-h-screen">

      {loading && (
        <div className="text-center text-lg">
          数据加载中...
        </div>
      )}

      {!loading && (

        <>
          {/* 标题 */}
          <div className="bg-white rounded-xl shadow p-6 mb-6">

            <h1 className="text-2xl font-bold mb-2">
              股票相似度分析
            </h1>

            <div className="text-gray-600">
              {similartyDto.stockName}
              （{similartyDto.stockCode}）
            </div>

          </div>

          {/* 统计卡片 */}
          <div className="grid grid-cols-4 gap-4 mb-6">

            <div className="bg-white rounded-xl shadow p-4">
              <div className="text-gray-500">
                交易日期
              </div>

              <div className="text-xl font-bold mt-2">
                {similartyDto.tradeDate}
              </div>
            </div>

            <div className="bg-white rounded-xl shadow p-4">
              <div className="text-gray-500">
                样本总数
              </div>

              <div className="text-2xl font-bold mt-2">
                {similartyDto.sampleNumber}
              </div>
            </div>

            <div className="bg-white rounded-xl shadow p-4">
              <div className="text-gray-500">
                成功样本
              </div>

              <div className="text-2xl font-bold text-green-600 mt-2">
                {successCount}
              </div>
            </div>

            <div className="bg-white rounded-xl shadow p-4">
              <div className="text-gray-500">
                失败样本
              </div>

              <div className="text-2xl font-bold text-red-500 mt-2">
                {failCount}
              </div>
            </div>

          </div>

          {/* 胜率 */}
          <div className="bg-white rounded-xl shadow p-6 mb-6">

            <div className="flex justify-between mb-2">

              <span className="font-medium">
                历史成功率
              </span>

              <span className="font-bold">
                {similartyDto.desc}
              </span>

            </div>

            <div className="w-full bg-gray-200 rounded-full h-8">

              <div
                className="bg-green-500 h-8 rounded-full text-white flex items-center justify-center"
                style={{
                  width:
                    `${similartyDto.winate * 100}%`
                }}
              >
                {(similartyDto.winate * 100).toFixed(2)}%
              </div>

            </div>

          </div>

          {/* 样本列表 */}
          <div className="bg-white rounded-xl shadow">

            <div className="p-4 border-b">

              <h2 className="text-xl font-bold">
                相似样本明细
              </h2>

            </div>

            <div className="overflow-x-auto">

              <table className="w-full text-sm">

                <thead>

                  <tr className="bg-gray-100">

                    <th className="p-3 text-left">
                      序号
                    </th>

                    <th className="p-3 text-left">
                      样本详情
                    </th>

                    <th className="p-3 text-center">
                      结果
                    </th>

                  </tr>

                </thead>

                <tbody>

                  {similartyDto.sampleList.map(
                    (item, index) => {

                      const success =
                        item.includes(
                          "成功=true"
                        );

                      return (

                        <tr
                          key={index}
                          className="border-b hover:bg-slate-50"
                        >

                          <td className="p-3">
                            {index + 1}
                          </td>

                          <td className="p-3 font-mono">
                            {item}
                          </td>

                          <td
                            className={
                              success
                                ? "p-3 text-center text-green-600 font-bold"
                                : "p-3 text-center text-red-500 font-bold"
                            }
                          >
                            {success
                              ? "✅成功"
                              : "❌失败"}
                          </td>

                        </tr>

                      );
                    }
                  )}

                </tbody>

              </table>

            </div>

          </div>
        </>
      )}

    </div>
  );
}

export default Similarity;