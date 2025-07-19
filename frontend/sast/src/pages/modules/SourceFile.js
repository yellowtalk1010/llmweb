import {Fragment, useState, useEffect } from "react"

function SourceFile({node}) {

    const file = node.path
    // const vtid = ""
    const [loading, setLoading] = useState(false);  //转圈圈加载进度条
    const [sourceCodeData, setSourceCodeData] = useState({
        lines:[],
        issues:[]
    }) //高亮文件内容

    const [options, setOptions] = useState({
        list:[]
    });
    const [selected, setSelected] = useState(null);
    const [selectedRule, setSelectedRule] = useState(null);

    // 加载数据
    useEffect(() => {
        fetch("/file_path?path=" + file, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        }).then(res => {
            const json = res.json()
            return json
        }).then(data => {
            console.log("下拉数据：", data);
            setOptions(data);
        }).catch(err => {
            console.error("加载失败", err);
        });
    }, [file]);
    

    useEffect(() => {

         if (!file) return;

        setLoading(true)

        fetch('/sourceCode_list?file='+file+'&vtid='+selected, {
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
    }, [file,selected]);


    const handleChange = (e) => {
        var vtid = e.target.value
        options.list.filter((item, index) => item.vtid==vtid).forEach((item,index) =>{
            console.info(">>>>>>>")
            console.info(item)
            setSelectedRule(item)
        })
        // .forEach((item, index) => {
        //     // console.info(">>>>>")
        //     // console.info(item)
        // })
        setSelected(e.target.value);
        console.log("你选择了：", e.target.value);
        console.log(e.target.value)
    };
  

    return (
    <>
      

      {loading ? (
        <div className="spinner"></div>
      ) : (
        <div>
            <div>
                <div>
                    <select id="myDropdown" value={selected} onChange={handleChange}>
                        <option key="" value="">-- 规则选择 --</option>
                        {options.list.map((item, idx) => (
                         <option key={item.vtid} value={item.vtid}>{item.rule}➖{item.ruleDesc}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <span>🔸{file}</span>
                </div>
                <div>
                    
                    {selectedRule && 
                    (
                        <>
                            <div><span>🔸{selectedRule.rule}</span></div>
                            <div><span>🔸{selectedRule.vtid}</span></div>
                            <div><span>🔸{selectedRule.defectLevel}</span></div>
                            <div><span>🔸{selectedRule.ruleDesc}</span></div>
                            <div><span>🔸问题数：{selectedRule.size}</span></div>
                        </>
                    )}
                </div>
                <hr></hr>
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