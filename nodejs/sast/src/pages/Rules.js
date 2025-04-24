import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"

function Rules() {
  const navigate = useNavigate();

  const [rulesData, setRulesData] = useState([])
  if(rulesData.length==0){
    fetch('/rules_list', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res =>{
      const json = res.json();
      // console.info(json)
      return json
    }).then(data =>{
      console.log("rules_list的数据")
      console.log(data)
      const ruleList = 
      data.map(item=>(
        item
      ))
      console.info(data)
      setRulesData(data)
    }).catch(e =>{
      console.log(e)
    })
  }

  return (
    <div id="rules">
      <h1>规则集</h1>

      <ul>
        {rulesData.map((rule, index) => (
          <li>
          <a href='rule?vtid={rule.vtid}'>{rule.vtid}</a> &nbsp;&nbsp;&nbsp;{rule.defectLevel}-{rule.size}&nbsp;/&nbsp;{rule.ruleDesc}<br></br>
          </li>
        ))}
      </ul>

    </div>
  );
}

export default Rules;
