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

    const handleSubmit = () => {
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
                <button onClick={handleSubmit}>查询</button>
            </div>


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
                    {stockDatas.data.map((row, index) => (
                        <tr
                        key={index}
                        style={{ cursor: "default" }}
                        onMouseEnter={e => e.currentTarget.style.backgroundColor = "#f5f5f5"}
                        onMouseLeave={e => e.currentTarget.style.backgroundColor = "#fff"}
                        >
                        <td className="td">
                            <div>
                            <button className="button">关注</button>
                            </div>
                            <div>
                            <span>{index + 1}</span>
                            </div>
                        </td>
                        <td className="td">
                            {row.name}
                            <a
                            href="#"
                            onClick={() => window.open("https://quote.eastmoney.com/" + row.code + ".html")}
                            >
                            {row.code}
                            </a>
                        </td>
                        <td className="td">
                            {row.name}
                        </td>
                        <td className="td">{row.name}</td>
                        <td className="td">{row.name}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
 
        </div>

        
    );
}

export default BiddingStock;
