import { useState, useEffect } from "react";

function AllStock() {
  const [stocks, setStocks] = useState([]);

  useEffect(() => {
    fetch('/stock/all?search=', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
      .then(res => res.json())
      .then(data => setStocks(data))
      .catch(e => console.error(e));
  }, []);

  // 样式对象
  const styles = {
    table: {
      borderCollapse: "collapse",
      width: "100%",
      fontFamily: "Arial, sans-serif",
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
    trHover: {
      backgroundColor: "#f9f9f9",
    },
    button: {
      padding: "4px 8px",
      backgroundColor: "#007bff",
      color: "#fff",
      border: "none",
      borderRadius: "4px",
      cursor: "pointer",
    }
  };

  return (
    <div style={{ padding: "20px" }}>
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
          {stocks.map((row, index) => (
            <tr
              key={index}
              style={{ cursor: "default" }}
              onMouseEnter={e => e.currentTarget.style.backgroundColor = "#f5f5f5"}
              onMouseLeave={e => e.currentTarget.style.backgroundColor = "#fff"}
            >
              <td style={styles.td}>
                <button style={styles.button}>关注</button>
              </td>
              <td style={styles.td}>{row.jys}</td>
              <td style={styles.td}>{row.api_code}</td>
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
