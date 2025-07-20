import {Fragment, useState, useEffect, useRef } from "react"

/**
 * @param  file 文件路径，根据文件可以获取当前文件违反了哪些规则类型
 * @param  selectRuleVtid 用户已经选择的规则vtid，和系统指定的vtid（这里为何要返回，是为了同步更新 select 下拉框）
 * @param  onSelectRuleVtid 函数，用来通过源代码模块，用户修改了vtid，需要重新加载
 */
function RulesTemplate({ file, selectRuleVtid, onSelectRuleVtid }) {

    const [options, setOptions] = useState({
        list:[] //记录下拉框中的数据
    });
    const [selectedItemVtid, setSelectedItemVtid] = useState(null);
    const [selectedItem, setSelectedItem] = useState(null);


    const mountedRef = useRef(false);
    useEffect(() => {
      if (!mountedRef.current) {
        mountedRef.current = true;

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
            if(selectRuleVtid!=null && selectRuleVtid!=""){
              data.list.filter((item, index) => item.vtid==selectRuleVtid).forEach((item,index) =>{
                  setSelectedItem(item)  //让规则下拉框选择原来的规则项目
              })
              onSelectRuleVtid(selectRuleVtid) //通知 源代码模块，vtid做了选择
              setSelectedItemVtid(selectRuleVtid)
            }
            else {
              //
            }
            
        }).catch(err => {
            console.error("加载失败", err);
        });
      }
    }, [file]);


    const handleChange = (e) => {
        var vtid = e.target.value
        console.log("你选择了：", vtid);
        if(vtid==""){
          onSelectRuleVtid(null)
          setSelectedItemVtid(null)
          setSelectedItem(null)
        }
        else{
          onSelectRuleVtid(vtid)
          setSelectedItemVtid(vtid)
          options.list.filter((item, index) => item.vtid==vtid).forEach((item,index) =>{
              setSelectedItem(item)
          })
        }
        
    };

  return (
    <div>
        <div>
            <select id="myDropdown" value={selectedItemVtid} onChange={handleChange} style={{ width: '100%' }}>
                <option key="" value="">-- 规则选择 --</option>
                {options.list.map((item, idx) => (
                  <option key={item.vtid} value={item.vtid}>
                    {item.size}➖{item.rule}➖{item.vtid}➖{item.ruleDesc}
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
                    <div>
                      <span>🔸总数:{selectedItem.size}</span>
                      <span>🔸{selectedItem.rule}</span>
                      <span>🔸{selectedItem.vtid}</span>
                      <span>🔸{selectedItem.defectLevel}</span>
                    </div>
                    
                    <div>
                      <span>🔸{selectedItem.ruleDesc}</span>
                    </div>
                    <div></div>
                </>
            )}
        </div>
        <hr></hr>
    </div>
  );
}


export default RulesTemplate;
