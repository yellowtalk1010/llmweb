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
        fetch("/stock/bidding?period="+period+"&type="+type, {
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
                <select value={period} id="period" >
                    <option value={0}>竞价抢筹</option>
                    <option value={1}>尾盘抢筹</option>
                </select>
                <select value={type} id="type">
                    <option value={1}>委托金额排序</option>
                    <option value={2}>成交金额排序</option>
                    <option value={3}>开盘金额顺序</option>
                    <option value={4}>抢筹涨幅排序</option>
                </select>
                <button onClick={handleSubmit}>查询</button>
            </div>
 
        </div>
    );
}

export default BiddingStock;
