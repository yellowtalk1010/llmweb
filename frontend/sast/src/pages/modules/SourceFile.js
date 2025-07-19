import {Fragment, useState, useEffect } from "react"

import Rules from './Rules';

function SourceFile({node}) {

    const file = node.path
    // const vtid = ""
    const [loading, setLoading] = useState(false);  //转圈圈加载进度条
    const [sourceCodeData, setSourceCodeData] = useState({
        lines:[],
        issues:[]
    }) //高亮文件内容

    useEffect(() => {

         if (!file) return;

        setLoading(true)

        fetch('/sourceCode_list?file='+file+'&vtid='+selectedVtid, {
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


    const [selectedVtid, setSelectedVtid] = useState("");  //选择的vtid
    const [issuesData, setIssuesData] = useState({
        lines:[],
        issues:[]
    }) //文件 + vtid 得到的 issue 列表

    // useEffect(() => {
    //     if (!file) return;

    //     console.info("选择的vtid是：" + selectedVtid)
        
    //     setLoading(true)

    //     fetch('/sourceCode_issue_list?file='+file+'&vtid='+selectedVtid, {
    //       method: 'GET',
    //       headers: {
    //         'Content-Type': 'application/json'
    //       }
    //     }).then(res =>{
    //         const json = res.json();
    //         return json
    //     }).then(data =>{
    //         console.info("请求issue")
    //         console.log(data)
    //         // setIssuesData(data)
    //     }).catch(e =>{
    //         console.log(e)
    //     }).finally(e=>{
    //         setLoading(false)
    //     })
    // }, [selectedVtid]);



    return (
    <>
      
      {loading ? (
        <div className="spinner"></div>
      ) : (
        <div>
            <div>
                <Rules file={file} onSelectRuleVtid={setSelectedVtid}></Rules>
            </div>
            <div>
                <ol>
                {sourceCodeData.lines.map((lineHtml, index) => (
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

