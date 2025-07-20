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

    const [selectedVtid, setSelectedVtid] = useState("");  //选择的vtid

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
    }, [file, selectedVtid]);


    //渲染issue列表
  function renderIssue1(lineIssues) {
    console.info("hello")
    return lineIssues.map((issue, index) => (
      <div id={issue.id} class="issueDiv">
        <div>{issue.name}</div>
        <div>
          {issue.line}/{issue.vtId}/{issue.rule}/{issue.defectLevel}/{issue.defectType}
        </div>
        <div>{issue.ruleDesc}</div>
        <div>{issue.issueDesc}</div>
      </div>
    ));
  }


    return (
    <>
      
      {loading ? (
        <div className="spinner"></div>
      ) : (
        <div>
            <div>
                <Rules file={file} vtid={selectedVtid} onSelectRuleVtid={setSelectedVtid}></Rules>
            </div>
            <div>
                <ol>
                {sourceCodeData.lines.map((lineHtml, index) => {
                    const lineNumber = index + 1;
                    const lineIssues = sourceCodeData.issues.filter(issue=>issue.line==lineNumber) || []
                    
                    const newLiElement = (
                        <span
                        key={index}
                        dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
                        />
                    );

                    if(lineIssues!=null && lineIssues.length > 0){
                        const divDoms = renderIssue1(lineIssues) //渲染issue列表,将issue对象编程react元素列表（不是dom元素列表）
                        // console.info("divDoms:")
                        // console.info(divDoms)
                        
                        const span = document.createElement('span'); //
                        span.innerHTML = lineHtml;
                        // console.info("span:")
                        //console.info(span.firstChild) //将html字符串转成dom元素
    
                        const newLiElement = (
                        <span
                        key={index}
                        dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
                        />
                        );
                        divDoms.unshift(newLiElement); //将新元素加入到列表中

                        return divDoms
                    }
                    else {
                        return newLiElement
                    }


                })}
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

