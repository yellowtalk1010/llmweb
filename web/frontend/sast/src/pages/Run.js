import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import 'react-tabs/style/react-tabs.css';

const Run = () => (
  <Tabs>
    <TabList>
      <Tab>Tab 1</Tab>
      <Tab>Tab 2</Tab>
      <Tab>Tab 3</Tab>
    </TabList>

    <TabPanel>
      <h2>Tab 1 内容</h2>
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
