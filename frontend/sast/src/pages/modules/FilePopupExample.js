import {Fragment, useState, useEffect, useRef } from "react"

function FilePopupExample({trace, onTrace}) {
    
    console.info("来吧展示")
    console.info(trace)
    const file = trace.file

    const [otherSourceCodeData, setOtherSourceCodeData] = useState({
        lines:[]
    })
    

    function markRedLine(line){
        console.info("添加红色标记：" + line)
        var floatingFileDom = document.getElementById("traceSourcecodeFile")
        console.info(floatingFileDom)
        var lineId = "otherfileline_" + line
        console.info(lineId)
        const otherFileLiDom = document.getElementById(lineId)
        console.info(otherFileLiDom)
        if(otherFileLiDom){
            const containerHeight = floatingFileDom.clientHeight;
            const liOffsetTop = otherFileLiDom.offsetTop;
            const liHeight = otherFileLiDom.offsetHeight;
            // 计算 li 要滚动到的位置，使其垂直居中
            const scrollTop = liOffsetTop - (containerHeight / 2) + (liHeight / 2);
            // 平滑滚动到目标位置
            floatingFileDom.scrollTo({
                top: scrollTop,
                behavior: 'smooth'
            });

            //将li标签加粗
            otherFileLiDom.classList.add('sourcecode-li');
        
            }
        
    }

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
                
                markRedLine(trace.line)  //给指定行好，添加红色标记
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
                <span>{file}</span>
                <button onClick={() => onTrace(null)}>关闭</button>
                </div>
                <div id="traceSourcecodeFile" style={styles.content}>
                    <ol>
                    {otherSourceCodeData.lines.map((lineHtml, index) => {
                        const lineNumber = index + 1;
                        const newLiElement = (
                            <>
                                <span
                                    key={index}
                                    dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
                                    />
                                    
                            </>
                        );
                        return newLiElement

                    })}
                    </ol>
                
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
    width: "1000px",
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
