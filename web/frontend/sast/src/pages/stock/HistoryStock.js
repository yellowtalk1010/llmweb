import { useState, useEffect } from "react";
import "./StockTable.css"

/***
 * 历史数据查询
 */
function HistoryStock() {
 
    const [search, setSearch] = useState("")  //查询的数据

    const [stockDatas, setStockDatas] = useState({
        code: "",
        msg: "",
        data: []
    });

  const getTodayDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}${month}${day}`;
  };
  //交易日期
  const [tradedate, setTradedate] = useState(getTodayDate());


    const handleSearch = () => {
        const search = document.getElementById("search").value
        console.info(search + ", " + tradedate)

        fetch("/historyStock/list?search="+search+"&tradedate="+tradedate, {
                method: "GET",
                headers: { "Content-Type": "application/json" }
            })
            .then(res => res.json())
            .then(data => {
                console.log("后端返回:", data);
                setStockDatas(data)
            })
            .catch(err => console.error("请求失败:", err));
    };

    
    return (
        <div className="container">
            {/* 查询表单 */}
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
                    id="search"
                    className="input"
                />
                <button onClick={handleSearch}>查询</button>
            </div>
            <div style={{ padding: "20px" }}>
                <table className="table">
                    <thead>
                    <tr>
                        <th className="th">交易日</th>
                        <th className="th">代码</th>
                        <th className="th">名称</th>
                        <th className="th">营业部名称</th>
                        <th className="th">买入额（万）</th>
                        <th className="th">买入占总成交比例</th>
                        <th className="th">卖出额（万）</th>
                        <th className="th">卖出占总成交比例</th>
                        <th className="th">净成交额（万）</th>
                        <th className="th">买卖类型</th>
                        <th className="th">上榜理由</th>
                    </tr>
                    </thead>

                    <tbody>
                    {stockDatas.data.map((row, index) => (
                        <tr
                        key={index}
                        style={{ cursor: "default" }}
                        onMouseEnter={e => e.currentTarget.style.backgroundColor = "#f5f5f5"}
                        onMouseLeave={e => e.currentTarget.style.backgroundColor = "#fff"}
                        >
                        <td className="td">{row.trade_date}</td>    
                        <td className="td">
                            {row.name}
                            <br/>
                            <a
                            href="#"
                            onClick={() => window.open("https://quote.eastmoney.com/" + row.ts_code + ".html")}
                            >
                            {row.ts_code}
                            </a>
                        </td>
                        <td className="td">{row.ts_code}名称【{row.count}】</td>
                        <td className="td">{row.exalter}</td>
                        <td className="td">{row.buy}</td>
                        <td className="td">{row.buy_rate}</td>
                        <td className="td">{row.sell}</td>
                        <td className="td">{row.sell_rate}</td>
                        <td className="td">{row.net_buy}</td>
                        <td className="td">{row.side_desc}</td>
                        <td className="td">{row.reason}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
 
        </div>

        
    );
}

export default HistoryStock;