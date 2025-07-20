import {Fragment, useState, useEffect } from "react"

import RulesTemplate from './RulesTemplate';
import IssuesTemplate from './IssuesTemplate';
import FilePopupExample from "./FilePopupExample";

function SourceFile({node, urlParamVtid}) {
    console.info("urlParamVtid:" + urlParamVtid)
    const file = node.path
    // const vtid = ""
    const [loading, setLoading] = useState(false);  //转圈圈加载进度条
    const [sourceCodeData, setSourceCodeData] = useState({
        lines:[],
        issues:[]
    }) //高亮文件内容

    const [selectedVtid, setSelectedVtid] = useState(urlParamVtid==null?"":urlParamVtid);  //用户在下拉框中选择的规则vtid，和 系统指定的vtid

    const [showPopup, setShowPopup] = useState(null);

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


    return (
    <>
      
      {loading ? (
        <div className="spinner"></div>
      ) : (
        <div id="SourceCode">
            <div>
                <RulesTemplate file={file} selectRuleVtid={selectedVtid} onSelectRuleVtid={setSelectedVtid}></RulesTemplate>
            </div>
            <div id="sourcecode_file">
                <ol>
                {sourceCodeData.lines.map((lineHtml, index) => {
                    const lineNumber = index + 1;
                    const lineIssues = sourceCodeData.issues.filter(issue=>issue.line==lineNumber) || []
                    const divDomsReact = <IssuesTemplate issueDatas={lineIssues} onShowPopup={setShowPopup}></IssuesTemplate>

                    const newLiElement = (
                        <>
                            <span
                                key={index}
                                dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
                                />
                                {divDomsReact}
                        </>
                    );

                    return newLiElement

                })}
                </ol>
            </div>
            {showPopup!=null && <FilePopupExample trace={showPopup} onTrace={setShowPopup} />}
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

