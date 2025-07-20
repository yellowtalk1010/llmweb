import {Fragment, useState, useEffect, useRef } from "react"

function FilePopupExample({trace, onTrace}) {
    
    console.info("来吧展示")
    console.info(trace)
    const file = trace.file
    
    const fileContent = `
    这是文件内容的第一行
    第二行
    第三行
    第四行
    第五行
    第六行
    第七行
    第八行
    这是文件内容的最后一行
    `.repeat(10); // 模拟长内容

    const [otherSourceCodeData, setOtherSourceCodeData] = useState({
        lines:[]
    })
    
    const mountedRef = useRef(false);
    useEffect(() => {

        if (!mountedRef.current) {
            
            mountedRef.current = true;

            fetch('/otherSourceCode_list?file='+file, {
                method: 'GET',
                headers: {
                'Content-Type': 'application/json'
                }
            }).then(res =>{
                const json = res.json();
                // console.info(json)
                return json
            }).then(data =>{
                // console.log("rule_vtid 的数据")
                console.log(data)
                setOtherSourceCodeData({
                lines: data
                })
                // console.info("渲染完成后执行")
            }).catch(e =>{
                console.log(e)
            })
        }

    },[file])
    

    return (
        <div>  
        
        {trace!=null && (
            <div style={styles.overlay}>
            <div style={styles.popup}>
                <div style={styles.header}>
                <span>文件内容</span>
                <button onClick={() => onTrace(null)}>关闭</button>
                </div>
                <div style={styles.content}>
                <pre style={styles.pre}>{fileContent}</pre>
                </div>
            </div>
            </div>
        )}
        </div>
    );

}

// 样式
const styles = {
  overlay: {
    position: "fixed",
    top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: "rgba(0,0,0,0.3)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    zIndex: 1000,
  },
  popup: {
    width: "600px",
    maxHeight: "80vh",
    backgroundColor: "white",
    borderRadius: "8px",
    overflow: "hidden",
    display: "flex",
    flexDirection: "column",
    boxShadow: "0 4px 12px rgba(0,0,0,0.2)"
  },
  header: {
    backgroundColor: "#f0f0f0",
    padding: "10px 15px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    position: "sticky",
    top: 0,
    zIndex: 10,
  },
  content: {
    padding: "10px 15px",
    overflowY: "auto",
    flex: 1,
  },
  pre: {
    whiteSpace: "pre-wrap",
    wordBreak: "break-word"
  }
};

export default FilePopupExample;
