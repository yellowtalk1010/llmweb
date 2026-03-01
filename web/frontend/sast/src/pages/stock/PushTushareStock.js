import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";

function PushTushareStock() {
  const [searchParams] = useSearchParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      const response = await fetch("/push_stocks/list");
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      const result = await response.json();
      console.info(result) //输出json结果
      setData(result);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // 首次加载立即执行
    fetchData();

    // 每30秒执行一次
    const interval = setInterval(() => {
      fetchData();
    }, 30000);

    // 组件卸载时清除定时器
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="p-6">
      <h1 className="text-xl font-bold mb-4">Push Tushare Stock</h1>

      {loading && <p>Loading...</p>}
      {error && <p className="text-red-500">Error: {error}</p>}

      {data && (
        <pre className="bg-gray-100 p-4 rounded-xl overflow-auto text-sm">
          {JSON.stringify(data, null, 2)}
        </pre>
      )}
    </div>
  );
}

export default PushTushareStock;