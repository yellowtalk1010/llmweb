import { Fragment, useState, useEffect, useRef } from "react";
import { Rnd } from "react-rnd";

/**
 * 用来弹出展示trace追踪的源代码，可拖动，可缩放，可关闭
 */
function FilePopupExample({ trace, onTrace }) {
  const file = trace?.file;
  const [otherSourceCodeData, setOtherSourceCodeData] = useState({ lines: [] });

  const mountedRef = useRef(false);
  const popupRef = useRef(null);

  useEffect(() => {
    if (!mountedRef.current) {
      mountedRef.current = true;
      fetch('/otherSourceCode_list?file=' + file)
        .then(res => res.json())
        .then(data => {
          setOtherSourceCodeData({ lines: data });
        })
        .catch(e => console.log(e));
    }
  }, [file]);

  useEffect(() => {
    if (otherSourceCodeData.lines.length > 0) {
      requestAnimationFrame(() => {
        markRedLine(trace.line);
      });
    }
  }, [otherSourceCodeData.lines, trace.line]);

  function markRedLine(line) {
    console.info("添加红色标记：" + line)
    const floatingFileDom = document.getElementById("traceSourcecodeFile");
    const otherFileLiDom = document.getElementById("otherfileline_" + line);
    if (otherFileLiDom && floatingFileDom) {
      const containerRect = floatingFileDom.getBoundingClientRect();
      const itemRect = otherFileLiDom.getBoundingClientRect();
      const containerScrollTop = floatingFileDom.scrollTop;
      const offset = itemRect.top - containerRect.top;
      const scrollTop = containerScrollTop + offset - (containerRect.height / 2) + (itemRect.height / 2);
      floatingFileDom.scrollTo({ top: scrollTop, behavior: 'smooth' });
      otherFileLiDom.classList.add("sourcecode-li");
    }
  }

  return (
    <>
      {trace && (
        <div style={styles.overlay}>
          <Rnd
            default={{
              x: (window.innerWidth - 1000) / 2,
              y: (window.innerHeight - 500) / 2,
              width: 1000,
              height: 500
            }}
            
            minWidth={1000}
            minHeight={300}
            style={styles.popupRnd}
          >
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
          </Rnd>
        </div>
      )}
    </>
  );
}

// 样式
const styles = {
  overlay: {
    position: "fixed",
    top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: "rgba(0,0,0,0.3)",
    zIndex: 1000
  },
  popupRnd: {
    backgroundColor: "white",
    borderRadius: "8px",
    display: "flex",
    flexDirection: "column",
    boxShadow: "0 4px 12px rgba(0,0,0,0.2)",
    overflow: "hidden"
  },
  header: {
    backgroundColor: "#f0f0f0",
    padding: "10px 15px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    position: "sticky",
    top: 0,
    zIndex: 10
  },
  content: {
    padding: "10px 15px",
    overflowY: "auto",
    flex: 1
  }
};

export default FilePopupExample;



// import {Fragment, useState, useEffect, useRef } from "react"

// function FilePopupExample({trace, onTrace}) {
    
//     console.info("来吧展示")
//     console.info(trace)
//     const file = trace.file

//     const [otherSourceCodeData, setOtherSourceCodeData] = useState({
//         lines:[]
//     })
    

//     function markRedLine(line){
//         console.info("添加红色标记：" + line)
//         var floatingFileDom = document.getElementById("traceSourcecodeFile")
//         console.info(floatingFileDom)
//         var lineId = "otherfileline_" + line
//         console.info(lineId)
//         const otherFileLiDom = document.getElementById(lineId)
//         console.info(otherFileLiDom)
//         if(otherFileLiDom){
//             const containerRect = floatingFileDom.getBoundingClientRect();
//             const itemRect = otherFileLiDom.getBoundingClientRect();

//             const containerScrollTop = floatingFileDom.scrollTop;
//             const offset = itemRect.top - containerRect.top;

//             const scrollTop = containerScrollTop + offset - (containerRect.height / 2) + (itemRect.height / 2);

//             floatingFileDom.scrollTo({
//                 top: scrollTop,
//                 behavior: 'smooth',
//             });

//             otherFileLiDom.classList.add("sourcecode-li");
        
//         }
        
//     }

//     const mountedRef = useRef(false);
//     useEffect(() => {

//         if (!mountedRef.current) {
            
//             mountedRef.current = true;

//             fetch('/otherSourceCode_list?file='+file, {
//                 method: 'GET',
//                 headers: {
//                 'Content-Type': 'application/json'
//                 }
//             }).then(res =>{
//                 const json = res.json();
//                 // console.info(json)
//                 return json
//             }).then(data =>{
//                 // console.log("rule_vtid 的数据")
//                 console.log(data)
//                 setOtherSourceCodeData({
//                     lines: data
//                 })
//                 // console.info("渲染完成后执行")
//             }).catch(e =>{
//                 console.log(e)
//             })
            
//         }

//     },[file])


//     useEffect(() => {
//         if (otherSourceCodeData.lines.length > 0) {
//             // 延迟一帧再执行，确保 DOM 已挂载
//             requestAnimationFrame(() => {
//                 markRedLine(trace.line);
//             });
//         }
//     }, [otherSourceCodeData.lines, trace.line]);

    

//     return (
//         <div>  
        
//         {trace!=null && (
//             <div style={styles.overlay}>
//             <div style={styles.popup}>
//                 <div style={styles.header}>
//                 <span>{file}</span>
//                 <button onClick={() => onTrace(null)}>关闭</button>
//                 </div>
//                 <div id="traceSourcecodeFile" style={styles.content}>
//                     <ol>
//                     {otherSourceCodeData.lines.map((lineHtml, index) => {
//                         const lineNumber = index + 1;
//                         const newLiElement = (
//                             <>
//                                 <span
//                                     key={index}
//                                     dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
//                                     />
                                    
//                             </>
//                         );
//                         return newLiElement

//                     })}
//                     </ol>
                
//                 </div>
//             </div>
//             </div>
//         )}
//         </div>
//     );

// }

// // 样式
// const styles = {
//   overlay: {
//     position: "fixed",
//     top: 0, left: 0, right: 0, bottom: 0,
//     backgroundColor: "rgba(0,0,0,0.3)",
//     display: "flex",
//     justifyContent: "center",
//     alignItems: "center",
//     zIndex: 1000,
//   },
//   popup: {
//     width: "1000px",
//     maxHeight: "50vh",
//     backgroundColor: "white",
//     borderRadius: "8px",
//     overflow: "hidden",
//     display: "flex",
//     flexDirection: "column",
//     boxShadow: "0 4px 12px rgba(0,0,0,0.2)"
//   },
//   header: {
//     backgroundColor: "#f0f0f0",
//     padding: "10px 15px",
//     display: "flex",
//     justifyContent: "space-between",
//     alignItems: "center",
//     position: "sticky",
//     top: 0,
//     zIndex: 10,
//   },
//   content: {
//     padding: "10px 15px",
//     overflowY: "auto",
//     flex: 1,
//   },
//   pre: {
//     whiteSpace: "pre-wrap",
//     wordBreak: "break-word"
//   }
// };

// export default FilePopupExample;
