import { useState, useEffect } from "react";
import "./StockTable.css";   // 引入CSS

function AllStock() {
  const [stockDatas, setStockDatas] = useState({
    stocks: [],
    blocks: []
  });

  useEffect(() => {
    fetch('/stock/my', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
      .then(res => res.json())
      .then(data => setStockDatas(data))
      .catch(e => console.error(e));
  }, []);

  const del = (event, api_code) => {
    fetch("/stock/delete?api_code=" + api_code, { method: 'GET' })
      .then(res => res.json())
      .then(data => console.log('成功', data))
      .catch(err => console.error('失败', err));
  };

  return (
    <div style={{ padding: "20px" }}>
      <table className="table">
        <thead>
          <tr>
            <th className="th">操作</th>
            <th className="th">归属</th>
            <th className="th">代码</th>
            <th className="th">名称</th>
            <th className="th">描述</th>
          </tr>
        </thead>
        <tbody>
          {stockDatas.stocks.map((row, index) => (
            <tr
              key={index}
              style={{ cursor: "default" }}
              onMouseEnter={e => e.currentTarget.style.backgroundColor = "#f5f5f5"}
              onMouseLeave={e => e.currentTarget.style.backgroundColor = "#fff"}
            >
              <td className="td">
                <div>
                  <button className="button" onClick={(e) => del(e, row.api_code)}>移除</button>
                </div>
                <div>
                  <span>{index + 1}</span>
                </div>
              </td>
              <td className="td">{row.jys}</td>
              <td className="td">
                <a
                  href="#"
                  onClick={() => window.open("https://quote.eastmoney.com/" + row.jys + row.api_code + ".html")}
                >
                  {row.api_code}
                </a>
              </td>
              <td className="td">{row.name}</td>
              <td className="td">{row.gl}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AllStock;
