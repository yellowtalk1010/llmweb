import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import 'react-tabs/style/react-tabs.css';

import RunTemplate from "./modules/RunTemplate";
import RunLogTemplate from './modules/RunLogTemplate';
import RunErrorTemplate from './modules/RunErrorTemplate';

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
          <RunLogTemplate></RunLogTemplate>
        </TabPanel>
        <TabPanel>
          <RunErrorTemplate></RunErrorTemplate>
        </TabPanel>
      </Tabs>
    </>
  );
}

export default Run;


