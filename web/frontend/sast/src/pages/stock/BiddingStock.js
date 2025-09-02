import { useState, useEffect } from "react";
import "./StockTable.css"

function BiddingStock() {

    const [period, setPeriod] = useState("1");
    const [type, setType] = useState("1");

    const [stockDatas, setStockDatas] = useState({
        code: "",
        msg: "",
        data: []
    });

    const merge = () => {
        const period = document.getElementById("period").value
        fetch("/stockBidding/merge?period="+period, {
                method: "GET",
                headers: { "Content-Type": "application/json" }
            })
            .then(res => res.json())
            .then(data => {
                console.log("后端返回:", data);
            })
            .catch(err => console.error("请求失败:", err));
    }


    const search = () => {
        const period = document.getElementById("period").value
        const type = document.getElementById("type").value
        console.info(period + ", " + type)
        fetch("/stockBidding/jjqc?period="+period+"&type="+type, {
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
                <select value={period} id="period" onChange={e=>setPeriod(e.target.value)}>
                    <option value={0}>竞价抢筹</option>
                    <option value={1}>尾盘抢筹</option>
                </select>
                <select value={type} id="type" onChange={e=>setType(e.target.value)}>
                    <option value={1}>委托金额排序</option>
                    <option value={2}>成交金额排序</option>
                    <option value={3}>开盘金额顺序</option>
                    <option value={4}>抢筹涨幅排序</option>
                </select>
                <input type="text" id="tradeDate"></input>
                <button onClick={search}>查询</button>
                
                <button onClick={merge}>合并</button>
            </div>


            <div style={{ padding: "20px" }}>
                <table className="table">
                    <thead>
                    <tr>
                        <th className="th">代码</th>
                        <th className="th">开盘金额</th>
                        <th className="th">抢筹涨幅</th>
                        <th className="th">抢筹成交额</th>
                        <th className="th">抢筹委托金额</th>
                        
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
                            <a
                            href="#"
                            onClick={() => window.open("https://quote.eastmoney.com/" + row.jys + row.code + ".html")}
                            >
                            {row.code}
                            </a>
                        </td>
                        <td className="td">{row.openAmt}</td>
                        <td className="td">{row.qczf}</td>
                        <td className="td">{row.qccje}</td>
                        <td className="td">{row.qcwtje}</td>
                        
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
 
        </div>

        
    );
}

export default BiddingStock;
