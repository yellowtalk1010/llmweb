import './../../index.css';
import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
// import "./StockTable.css"

/***
 * 历史数据查询
 */
function TopInst() {

    const [searchParams] = useSearchParams();
 
    //交易日期
    const [tradedate, setTradedate] = useState("");
    //查询的数据
    const [search, setSearch] = useState("")  


    useEffect(() => {
        const tsCode = searchParams.get("ts_code");
        console.info(tsCode)
        if (tsCode) {
            setTradedate("")
            setSearch(tsCode);
            handleSearch(tsCode, "");
        }
    }, []);

    const [stockDatas, setStockDatas] = useState({
        code: "",
        msg: "",
        data: []
    });

    const handleSearch = (searchValue = search, tradeDateValue = tradedate) => {
        console.info(searchValue + ", " + tradeDateValue)
        fetch("/top_inst/list?search="+searchValue+"&tradedate="+tradeDateValue, {
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
                    value={tradedate}
                    onChange={(e) => setTradedate(e.target.value)}
                    className="border rounded px-3 py-2"
                />
                <input
                    type="text"
                    id="search"
                    className="w-full md:w-80 px-4 py-2 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
                <button 
                    onClick={() => handleSearch(search, tradedate)}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
                >
                    查询
                </button>
            </div>
            <div style={{ padding: "20px" }}>
                <table className="table">
                    <thead>
                    <tr className="bg-gray-100 border-b">
                        <th className="p-3 text-left border border-gray-200">交易日</th>
                        <th className="p-3 text-left border border-gray-200">代码</th>
                        <th className="p-3 text-left border border-gray-200">名称</th>
                        <th className="p-3 text-left border border-gray-200">买入额（万）</th>
                        <th className="p-3 text-left border border-gray-200">买入占总成交比例</th>
                        <th className="p-3 text-left border border-gray-200">卖出额（万）</th>
                        <th className="p-3 text-left border border-gray-200">卖出占总成交比例</th>
                        <th className="p-3 text-left border border-gray-200">净成交额（万）</th>
                        <th className="p-3 text-left border border-gray-200">买卖类型</th>
                        <th className="p-3 text-left border border-gray-200">上榜理由</th>
                        <th className="p-3 text-left border border-gray-200">营业部名称</th>
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
                            {index}
                            <br/>
                            {row.trade_date}
                        </td>    
                        <td className="td">
                            {row.ts_name}
                            <br/>
                            <a
                            href="#"
                            onClick={() => window.open("https://quote.eastmoney.com/" + row.s_1 + row.s_0 + ".html")}
                            >
                            {row.ts_code}
                            </a>
                        </td>
                        <td className="td">{row.ts_name}【{row.count}】<br/>{row.hm_name}</td>
                        <td className="td">{row.buy}【{row.buyDesc}】</td>
                        <td className="td">{row.buy_rate}</td>
                        <td className="td">{row.sell}【{row.sellDesc}】</td>
                        <td className="td">{row.sell_rate}</td>
                        <td className="td">{row.net_buy}【{row.netBuyDesc}】</td>
                        <td className="td">{row.side_desc}</td>
                        <td className="td">{row.reason}</td>
                        <td className="td">{row.exalter}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
 
        </div>

        
    );
}

export default TopInst;