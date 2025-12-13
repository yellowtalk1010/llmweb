import { useState, useEffect } from "react";
import "./StockTable.css";

function AllStock() {
  const [stockDatas, setStockDatas] = useState({
    stocks: [],
    blocks: []
  });
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("");

  useEffect(() => {
    fetch('/stock/all?search=', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
      .then(res => res.json())
      .then(data => setStockDatas(data))
      .catch(e => console.error(e));
  }, []);

  // 查询事件
  const handleSearch = () => {
    let url = `/stock/all?search=${encodeURIComponent(search)}`;
    if (filter) {
      url += `&filter=${encodeURIComponent(filter)}`;
    }
    fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
      .then(res => res.json())
      .then(data => setStockDatas(data))
      .catch(e => console.error(e));
  };

  const add = (event, api_code) => {
    fetch("/stock/add?api_code=" + api_code, { method: 'GET' })
      .then(res => res.json())
      .then(data => console.log('成功', data))
      .catch(err => console.error('失败', err));
  };


  const getTodayDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}${month}${day}`;
  };
  //交易日期
  const [tradedate, setTradedate] = useState(getTodayDate());


  return (
    <div className="container">
      <div className="form">
        <input
          type="date"
          value={`${tradedate.slice(0, 4)}-${tradedate.slice(4, 6)}-${tradedate.slice(6, 8)}`}
          onChange={(e) => {
            const value = e.target.value.replace(/-/g, '');
            setTradedate(value);
          }}
          className="input"
        />
        <input
          type="text"
          placeholder="请输入代码"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="input"
        />
        <button onClick={handleSearch} className="button">查询</button>
      </div>

      {/* 表格 */}
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
                  <button className="button" onClick={(e) => add(e, row.api_code)}>关注</button>
                </div>
                <div>
                  <span>{index + 1}</span>
                </div>
              </td>
              <td className="td">{row.jys}</td>
              <td className="td">
                <a
                  href=""
                  onClick={(e) => {
                     e.preventDefault(); //// 阻止页面跳到顶部
                    window.open("https://quote.eastmoney.com/" + row.jys + row.api_code + ".html");
                  }}
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
