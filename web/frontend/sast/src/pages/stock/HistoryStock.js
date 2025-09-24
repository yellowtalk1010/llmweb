import { useState, useEffect } from "react";
import "./StockTable.css"

/***
 * 历史数据查询
 */
function HistoryStock() {
 
    const [context, setContext] = useState("")  //查询的数据
    const [tradeDate, setTradeDate] = useState(""); // 新增状态用于存储日期

    const [stockDatas, setStockDatas] = useState({
        code: "",
        msg: "",
        data: []
    });

    // 设置默认日期为今天
    useEffect(() => {
        const today = new Date();
        const formattedDate = today.toISOString().split('T')[0];
        setTradeDate(formattedDate);
    }, []);


    const search = () => {
        const context = document.getElementById("context").value
        console.info(context + ", " + tradeDate)

        fetch("/historyStock/list?context="+context+"&tradeDate="+tradeDate, {
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
                    type="text"
                    id="context"
                ></input>
                &nbsp;
                {/* 修改为日期选择器 */}
                <input 
                    type="date" 
                    id="tradeDate" 
                    value={tradeDate}
                    onChange={e => setTradeDate(e.target.value)}
                ></input>
                &nbsp;
                <button onClick={search}>查询</button>
            </div>


            <div style={{ padding: "20px" }}>
                <table className="table">
                    <thead>
                    <tr>
                        <th className="th">名称/代码</th>
                        <th className="th">时间</th>
                        <th className="th">开盘价</th>
                        <th className="th">最低价</th>
                        <th className="th">最高价</th>
                        <th className="th">收盘价</th>
                        <th className="th">换手率</th>
                        <th className="th">涨跌</th>
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
                        <td className="td">
                            {row.name}
                            <br/>
                            <a
                            href="#"
                            onClick={() => window.open("https://quote.eastmoney.com/" + row.jys + row.api_code + ".html")}
                            >
                            {row.api_code}
                            </a>
                        </td>
                        <td className="td">{row.time}</td>
                        <td className="td">{row.open}</td>
                        <td className="td">{row.low}</td>
                        <td className="td">{row.high}</td>
                        <td className="td">{row.close}</td>
                        <td className="td">{row.turnoverRatio}</td>
                        <td className="td">{row.changeRatio}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
 
        </div>

        
    );
}

export default HistoryStock;