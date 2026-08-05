import './../../index.css';
import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";

/***
 * 查看股票龙虎榜数据
 */
function TopInst() {

    const [searchParams] = useSearchParams();
 
    //交易日期
    const [tradedate, setTradedate] = useState("");
    //查询的数据
    const [search, setSearch] = useState("")  
    // 是否聚合
    const [group, setGroup] = useState(false);

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
        data: [],
        date: []
    });

    const handleSearch = (searchValue = search, tradeDateValue = tradedate) => {
        console.info(searchValue + ", " + tradeDateValue + ", " + group)
        fetch("/top_inst/list?search="+searchValue+"&tradedate="+tradeDateValue+"&group="+group, {
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
                <select
                    value={tradedate}
                    onChange={(e) => setTradedate(e.target.value)}
                    className="border rounded px-3 py-2"
                >
                    <option value=""></option>
                    {
                        (stockDatas.date || []).map((date, index) => (
                            <option key={index} value={date}>
                                {date}
                            </option>
                        ))
                    }
                </select>
                <input
                    type="text"
                    id="search"
                    className="w-full md:w-80 px-4 py-2 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
                <label className="  items-center gap-1">
                    <input
                        type="checkbox"
                        checked={group}
                        onChange={(e) => setGroup(e.target.checked)}
                    />
                    聚合
                </label>
                <button 
                    onClick={() => handleSearch(search, tradedate)}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
                >
                    查询
                </button>
            </div>
            <div style={{ padding: "20px" }}>
                <table className="w-full text-sm border-collapse bg-white">
                    <thead>
                    <tr className="border-b bg-gray-100 hover:bg-gray-100">
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
                            className="border-b group"
                        >
                        <td className="p-2">
                            {index}
                            <br/>
                            {row.trade_date}
                        </td>    
                        <td className="p-2">
                            <a
                            href="#"
                            onClick={() => window.open(row.easyMoneyURL)}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-blue-600 underline"
                            >
                            {row.ts_name}
                            <br/>
                            {row.ts_code}
                            </a>
                        </td>
                        <td className="p-2">{row.ts_name}【{row.count}】<br/>{row.hm_name}</td>
                        <td className="p-2">{row.buy}【{row.buyDesc}】</td>
                        <td className="p-2">{row.buy_rate}</td>
                        <td className="p-2">{row.sell}【{row.sellDesc}】</td>
                        <td className="p-2">{row.sell_rate}</td>
                        <td className="p-2">{row.net_buy}【{row.netBuyDesc}】</td>
                        <td className="p-2">{row.side_desc}</td>
                        <td className="p-2">{row.reason}</td>
                        <td className="p-2">{row.exalter}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
 
        </div>

        
    );
}

export default TopInst;