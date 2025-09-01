import { useState, useEffect } from "react";

function AllStock() {
  const [stockDatas, setStockDatas] = useState({
    stocks:[],
    blocks:[]
  });
  const [search, setSearch] = useState("");      // 输入框
  const [filter, setFilter] = useState("");      // 下拉框选择

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
    console.info(api_code)
    fetch("/stock/add?api_code=" + api_code, {
        method: 'GET',
    })
    .then(res => res.json())
    .then(data => console.log('成功', data))
    .catch(err => console.error('失败', err));

  }

  // 样式对象
  const styles = {
    table: {
      borderCollapse: "collapse",
      width: "100%",
      fontFamily: "Arial, sans-serif",
      marginTop: "10px"
    },
    th: {
      border: "1px solid #ccc",
      padding: "8px",
      backgroundColor: "#f0f0f0",
      fontWeight: "bold",
      textAlign: "center",
    },
    td: {
      border: "1px solid #ccc",
      padding: "8px",
      textAlign: "center",
    },
    button: {
      padding: "4px 12px",
      backgroundColor: "#007bff",
      color: "#fff",
      border: "none",
      borderRadius: "4px",
      cursor: "pointer",
      marginLeft: "8px"
    },
    input: {
      padding: "6px 10px",
      marginRight: "8px",
      border: "1px solid #ccc",
      borderRadius: "4px",
    },
    select: {
      padding: "6px 10px",
      marginRight: "8px",
      border: "1px solid #ccc",
      borderRadius: "4px",
    },
    form: {
      marginBottom: "10px"
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      {/* 查询表单 */}
      <div style={styles.form}>
        <textarea
          type="text"
          placeholder="请输入代码"
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={styles.input}
        />
        
        <button onClick={handleSearch} style={styles.button}>查询</button>
      </div>

      {/* 表格 */}
      <table style={styles.table}>
        <thead>
          <tr>
            <th style={styles.th}>操作</th>
            <th style={styles.th}>归属</th>
            <th style={styles.th}>代码</th>
            <th style={styles.th}>名称</th>
            <th style={styles.th}>描述</th>
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
              <td style={styles.td}>
                <div>
                    <button style={styles.button} onClick={(e)=>add(e, row.api_code)}>关注</button>
                </div>
                <div>
                    <span>{index+1}</span>
                </div>
              </td>
              <td style={styles.td}>{row.jys}</td>
              <td style={styles.td}>
                <a href="#" onClick={()=>window.open("https://quote.eastmoney.com/"+row.jys + row.api_code+".html")}>{row.api_code}</a>
              </td>
              <td style={styles.td}>{row.name}</td>
              <td style={styles.td}>{row.gl}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AllStock;
