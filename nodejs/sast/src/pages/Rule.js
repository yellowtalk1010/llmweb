import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';

function Rule() {

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const vtid = queryParams.get('vtid');

  console.info(vtid)

  const [ruleData, setRuleData] = useState({
    list:[],
    defectLevel: '',
    ruleDesc: '',
    status: 0
  })
  if(ruleData.status==0){
    fetch('/rule_vtid?vtid='+vtid, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res =>{
      const json = res.json();
      // console.info(json)
      return json
    }).then(data =>{
      console.log("rule_vtid 的数据")
      console.log(data)
      setRuleData({
        ...data,
        status: 200
      })
    }).catch(e =>{
      console.log(e)
    })
  }

  return (
    <div id="rules">
      {ruleData.defectLevel}/{ruleData.ruleDesc}
      <ul>
        {ruleData.list.map((rule, index) => (
            <li>
              <a href="sourceCode?vtid=SYSTEM_CONSTRAINTS_01&amp;file=D:/development/github/engine/standardCheckers/cj2000a/src/test/resources/cj2000a/Rule2.c">{rule.file}</a> &nbsp;&nbsp;&nbsp;{rule.size}
            </li>
        ))}
      </ul>

    </div>
  );
}

export default Rule;
