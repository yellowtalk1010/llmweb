import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"

import styles from './Rules.css';

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

      <table style={{ width: '100%', borderCollapse: 'collapse', tableLayout: 'auto' }}>
        <tbody>
          {rulesData.list.map((rule, index) => (
          <tr key={index}>
            <td className={`highlight`}><a href={`rule?vtid=${rule.vtid}`}>{rule.vtid}</a></td>
            <td className={`highlight`}>{rule.rule}</td>
            <td className={`highlight`}>{rule.size}</td>
            <td className={`highlight`}>{rule.defectLevel}</td>
            <td className="highlight">{rule.ruleDesc}</td>
          </tr>
        ))}
        </tbody>
      </table>

    </div>

  );
}


export default Rules;
