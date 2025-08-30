import React, { useState } from "react";

function DynamicTable({ type, columns, datas, apiUrl }) {
  const [data, setData] = useState(datas || []);

  const handleAction = async (row, index) => {
    try {
      if (type === "add") {
        const resp = await fetch(apiUrl, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(row),
        });
        const result = await resp.json();
        if (result.success) {
          alert("添加成功");
        }
      } else if (type === "delete") {
        const resp = await fetch(`${apiUrl}/${row.id}`, { method: "DELETE" });
        const result = await resp.json();
        if (result.success) {
          alert("删除成功");
          setData(data.filter((_, i) => i !== index));
        }
      }
    } catch (err) {
      console.error(err);
      alert("操作失败");
    }
  };

  return (
    <div>
      <table style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead>
          <tr style={{ backgroundColor: "#f0f0f0", fontWeight: "bold" }}>
            <th style={{ border: "1px solid black", padding: "4px" }}>
              {type === "add" ? "添加" : "删除"}
            </th>
            {columns.map((col) => (
              <th
                key={col}
                style={{ border: "1px solid black", padding: "4px" }}
              >
                {col}
              </th>
            ))}
          </tr>
        </thead>

        <tbody>
          {data.map((row, index) => (
            <tr key={index}>
              <td style={{ border: "1px solid black", padding: "4px" }}>
                <button onClick={() => handleAction(row, index)}>
                  {type === "add" ? "添加" : "删除"}
                </button>
              </td>
              {columns.map((col) => (
                <td
                  key={col}
                  style={{ border: "1px solid black", padding: "4px" }}
                >
                  {row[col]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default DynamicTable;
