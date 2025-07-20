import { useNavigate } from 'react-router-dom';
import {Fragment, useState, useEffect, useRef } from "react"

import styles from './AllRules.css';

function AllRules() {
  const navigate = useNavigate();

  const [rulesData, setRulesData] = useState([])
  
  const mountedRef = useRef(false);
  useEffect(() => {
    if (!mountedRef.current) {
        mountedRef.current = true;
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
          setRulesData(data)
        }).catch(e =>{
          console.log(e)
        })
    }

  },[rulesData])


  return (
    <div id="rules">

      <table style={{ width: '100%', borderCollapse: 'collapse', tableLayout: 'auto' }}>
        <tbody>
          <tr>
            <td className={`highlight`}><a style={{ textDecoration: 'none' }} href={`/pages/AllFiles`}>全部规则</a></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
          </tr>
          {rulesData.map((rule, index) => (
          <tr key={index}>
            <td className={`highlight`}><a style={{ textDecoration: 'none' }} href={`/pages/AllFiles?vtid=${rule.vtid}`}>{rule.vtid}</a></td>
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


export default AllRules;
