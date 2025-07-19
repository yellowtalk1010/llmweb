import {Fragment, useState, useEffect } from "react"

function SourceFile({node}) {

    const file = node.path
    const [loading, setLoading] = useState(false);  //转圈圈加载进度条
    const [sourceCodeData, setSourceCodeData] = useState([]) //高亮文件内容
    

    useEffect(() => {

         if (!file) return;

        setLoading(true)

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
          setSourceCodeData(data)
          // console.info("渲染完成后执行")
        }).catch(e =>{
          console.log(e)
        }).finally(e=>{
            setLoading(false)
        })
    }, [file]);

  

    return (
    <>
      

      {loading ? (
        <div className="spinner"></div>
      ) : (
        <div>
            <div>
                <span>{file}</span>
            </div>
            <div>
                <ol>
                {sourceCodeData.map((lineHtml, index) => (
                    <span
                    key={index}
                    dangerouslySetInnerHTML={{ __html: lineHtml }}
                    />
                ))}
                </ol>
            </div>
        </div>
      )}

      {/* Spinner 样式 */}
      <style>
        {`
          .spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #3498db;
            border-radius: 50%;
            width: 36px;
            height: 36px;
            animation: spin 1s linear infinite;
            margin: 20px auto;
          }
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
    </>
  );
}


export default SourceFile;