import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import 'react-tabs/style/react-tabs.css';

const Run = () => (
  <Tabs>
    <TabList>
      <Tab>run</Tab>
      <Tab>log</Tab>
      <Tab>error</Tab>
    </TabList>

    <TabPanel>
      
      <form>
        <div>
          <div>
            <button>运行</button>
          </div>
          <div>
            <span>命令:</span>
          </div>
          <input type='text'></input>
        </div>
        <div>
          <div>
            <span>参数:</span>
          </div>
          <textarea></textarea>
        </div>
      </form>
    </TabPanel>
    <TabPanel>
      <h2>Tab 2 内容</h2>
    </TabPanel>
    <TabPanel>
      <h2>Tab 3 内容</h2>
    </TabPanel>
  </Tabs>
);

export default Run;
