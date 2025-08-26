import {Fragment, useState, useEffect } from "react"

/**
 * @param: issueDatas 是跟随 文件返回 issue列表数据
 */
function IssuesTemplate({ issueDatas, onShowPopup }) {
  // console.info("触发issues模块渲染")
  // console.info(issueDatas)

  if(issueDatas==null || Array.from(issueDatas).length==0){
    return null;
  }


  function link(event, currentFile, trace){
    console.info(trace)
    document.getElementById("sourcecode_file").querySelectorAll('div a.trace_a_click').forEach(li => {
      li.classList.remove('trace_a_click');
    });
    document.getElementById("sourcecode_file").querySelectorAll('div li.sourcecode-li').forEach(li => {
      li.classList.remove('sourcecode-li');
    });

    event.target.classList.add('trace_a_click')

    const toId = trace.id
    const toFile = trace.file
    if(currentFile==toFile){
      //closeFloatingFile()
      //同一个文件中
      const el = document.getElementById("line_" + trace.line)
      el.scrollIntoView({
        behavior: 'smooth',      // 平滑滚动
        block: 'center',         // 垂直方向：滚动到中间
        inline: 'center'         // 水平方向：滚动到中间
      });

      el.classList.add('sourcecode-li')
      
      console.info("统一文件中打开")
    }
    else{
      //不同文件中打开
      // openFloatingFile(event, trace)
      console.info("不同文件中打开")

      onShowPopup(trace)
    }

    
      
  }

  //渲染issue
  function renderIssue(issue) {
    return (
      <div key={issue.id} id={issue.id} className="issueDiv">
        <div>{issue.id}</div>
        <div>{issue.name}</div>
        <div>
          {issue.line}/{issue.vtId}/{issue.rule}/{issue.defectLevel}/{issue.defectType}
        </div>
        <div>{issue.ruleDesc}</div>
        <div>{issue.issueDesc!=null?"描述："+issue.issueDesc:null}</div>
        <div>
        {
          issue.traces.map((trace, traceIndex) => (
            <div>
              <a className="trace_a" onClick={(event)=>link(event, issue.filePath, trace)} >{trace.file} # {trace.line} # {trace.message}</a>
            </div>
          ))
        }
        </div>
        <a class="ai_check" id={issue.id}>AI审计</a>
      </div>
    );
  }


//渲染functonModule函数建模
function renderFunctionModule(issue) {
  return (
    <div key={issue.id} id={issue.id} className="issueDiv">
      <div>{issue.id}</div>
      <div>{issue.data.funcLine}，函数输入输出</div>
      <div>
        <table>
          <tbody>
            <tr>
              <td>
                {issue.data.funcName}
                &nbsp;&nbsp;&nbsp;
                函数
              </td>
              {issue.data.params.map((param, index) => (
                <ParamItem 
                  key={index}
                  param={param}
                  issueId={issue.id}
                />
              ))}
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}


  return issueDatas != null
    ? issueDatas.map((issue) => {
      console.info(issue)
      if(issue.vtId=="FunctionModule"){
        return renderFunctionModule(issue)
      }
      else{
        return renderIssue(issue)
      }
    })
    : null;
}


export default IssuesTemplate;







function ParamItem({ param, issueId }) {
  const [isHighlighted, setIsHighlighted] = useState(false);


    //提交函数建模数据
  const handleFunctionModule = async (e, issueId) => {
    console.info("建模id：" + issueId)

    const issueDiv = document.getElementById(issueId);
  
    // 2. 获取该 div 下所有 select.param 元素
    const paramSelects = issueDiv.querySelectorAll("select.param");
    
    // 3. 提取它们的值
    const paramValues = Array.from(paramSelects).map(select => select.value);
    
    console.log("Issue ID:", issueId);
    console.log("Param values:", paramValues);

    try {
      const response = await fetch("/handle_func_module", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          issueId: issueId,
          paramValues: paramValues
        }),
      });

      if (!response.ok) {
        throw new Error("请求失败");
      }
      
      const result = await response.json();
      console.log("后端返回数据:", result);
      
      if(result.status=="success"){
        //成功，则回写
        const paramValues = result.data.data.params;
        console.info("实际数据" + paramValues)
        paramSelects.forEach((select, index) => {
          const paramValue = paramValues[index]
          select.value = paramValue.in_out || ""; // 如果值为 null，设为空字符串
        });
      }
      else {
        alert(result.status)
      }
    } catch (error) {
      console.error("提交出错:", error);
      alert("提交失败，请重试！");
    }
  }

  const handleLocalChange = (e) => {
    handleFunctionModule(e, issueId);
    setIsHighlighted(true);
    setTimeout(() => setIsHighlighted(false), 2000);
  };

  return (
    <td>
      <span style={{ 
        display: 'inline-block', // 确保transform能正常工作
        color: isHighlighted ? '#28a745' : 'inherit',
        fontSize: isHighlighted ? '1.2em' : '1em',
        transform: isHighlighted ? 'scale(1.1)' : 'scale(1)',
        transition: 'all 0.5s ease-out',
        fontWeight: isHighlighted ? 'bold' : 'normal'
      }}>
        {param.param}
      </span>
      &nbsp;&nbsp;&nbsp;
      <select 
        className="param" 
        value={param.in_out} 
        onChange={handleLocalChange}
      >
        <option value={null}></option>
        <option value="in">输入</option>
        <option value="out">输出</option>
      </select>
    </td>
  );
}
