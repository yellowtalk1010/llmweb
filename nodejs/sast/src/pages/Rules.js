import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"

function Rules() {
  const navigate = useNavigate();

  const [rulesData, setRulesData] = useState({list:[], status: 0})
  if(rulesData.status==0){
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
      setRulesData({
        list: data,
        status: 200
      })
    }).catch(e =>{
      console.log(e)
    })
  }

  return (
    <div id="rules">
      <ul>
        {rulesData.list.map((rule, index) => (
          <li>
            <a href={`rule?vtid=${rule.vtid}`}>{rule.vtid}</a> &nbsp;&nbsp;&nbsp;{rule.defectLevel}-{rule.size}&nbsp;/&nbsp;{rule.ruleDesc}
          </li>
        ))}
      </ul>

    </div>
  );
}

export default Rules;
