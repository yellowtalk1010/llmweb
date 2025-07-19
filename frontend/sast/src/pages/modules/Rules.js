import {Fragment, useState, useEffect, useRef } from "react"

function Rules({ file, myVtid, onSelectRuleVtid }) {

    const [options, setOptions] = useState({
        list:[] //记录下拉框中的数据
    });
    const [selectedItemVtid, setSelectedItemVtid] = useState(null);
    const [selectedItem, setSelectedItem] = useState(null);


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


    const handleChange = (e) => {
        var vtid = e.target.value
        console.log("你选择了：", vtid);
        onSelectRuleVtid(vtid)
        setSelectedItemVtid(vtid)
        options.list.filter((item, index) => item.vtid==vtid).forEach((item,index) =>{
            setSelectedItem(item)
        })
    };

  return (
    <div>
        <div>
            <select id="myDropdown" value={selectedItemVtid} onChange={handleChange} style={{ width: '100%' }}>
                <option key="" value="">-- 规则选择 --</option>
                {options.list.map((item, idx) => (
                  <option key={item.vtid} value={item.vtid}>
                    {item.size}➖{item.rule}➖{item.ruleDesc}
                  </option>
                ))}
            </select>
        </div>
        <div>
            <span>🔸{file}</span>
        </div>
        <div>
            
            {selectedItem && 
            (
                <>
                    <div><span>🔸{selectedItem.rule}</span></div>
                    <div><span>🔸{selectedItem.vtid}</span></div>
                    <div><span>🔸{selectedItem.defectLevel}</span></div>
                    <div><span>🔸{selectedItem.ruleDesc}</span></div>
                    <div><span>🔸问题数：{selectedItem.size}</span></div>
                </>
            )}
        </div>
        <hr></hr>
    </div>
  );
}


export default Rules;
