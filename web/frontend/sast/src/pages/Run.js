import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import 'react-tabs/style/react-tabs.css';


import RunTemplate from "./modules/RunTemplate";

function Run() {

  return (
    <>
      <Tabs>
        <TabList>
          <Tab>run</Tab>
          <Tab>log</Tab>
          <Tab>error</Tab>
        </TabList>

        <TabPanel>
          
          <RunTemplate></RunTemplate>
        </TabPanel>
        <TabPanel>
          <h2>Tab 2 内容</h2>
        </TabPanel>
        <TabPanel>
          <h2>Tab 3 内容</h2>
        </TabPanel>
      </Tabs>
    </>
  );
}

export default Run;


