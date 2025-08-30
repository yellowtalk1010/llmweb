import {Fragment, useState, useEffect } from "react"

function AllStock() {

    const [stocks, setStocks] = useState([]);

    useEffect(() => {
        fetch('/stock/all?search=', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        }).then(res =>{
          const json = res.json();
          return json
        }).then(data =>{
          console.log(data)
          setStocks(data)
        }).catch(e =>{
          //console.log(e)
        }).finally(e=>{
            //
        })
    }, []);

    return (
        <div>
      <table style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead>
          <tr style={{ backgroundColor: "#f0f0f0", fontWeight: "bold" }}>
            <th style={{ border: "1px solid black", padding: "4px" }}>
                操作
            </th>
            <th style={{ border: "1px solid black", padding: "4px" }}>
                归属    
            </th>
            <th style={{ border: "1px solid black", padding: "4px" }}>
                代码    
            </th>
            <th style={{ border: "1px solid black", padding: "4px" }}>
                名称    
            </th>
            <th style={{ border: "1px solid black", padding: "4px" }}>
                描述    
            </th>
          </tr>
        </thead>

        <tbody>
          {stocks.map((row, index) => (
            <tr key={index}>
              <td style={{ border: "1px solid black", padding: "4px" }}>
                关注
              </td>
              <td
                  style={{ border: "1px solid black", padding: "4px" }}
                >
                    {row.jys}
                </td>
                <td
                  style={{ border: "1px solid black", padding: "4px" }}
                >
                    {row.api_code}
                </td>
                <td
                  style={{ border: "1px solid black", padding: "4px" }}
                >
                    {row.name}
                </td>
                <td
                  style={{ border: "1px solid black", padding: "4px" }}
                >
                    {row.gl}
                </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    );
}


export default AllStock;