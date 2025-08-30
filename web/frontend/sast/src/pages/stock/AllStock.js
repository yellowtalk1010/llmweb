import React, { useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";

const pages = [
  { id: "1", title: "Google", url: "https://www.baidu.com" },
  { id: "2", title: "Bing", url: "https://www.ifeng.com" },
];

const AllStock = () => {
  const [openedPages, setOpenedPages] = useState(pages.slice(0, 1)); // 默认打开第一个
  const [collapsed, setCollapsed] = useState(false);

  const openPage = (page) => {
    if (!openedPages.find((p) => p.id === page.id)) {
      setOpenedPages([...openedPages, page]);
    }
  };

  return (
    <div style={{ height: "100vh" }}>
      <PanelGroup direction="horizontal">
        {/* 左边栏 */}
        <Panel defaultSize={20} minSize={5} maxSize={50}>
          <div style={{ padding: 10, height: "100%", boxSizing: "border-box", borderRight: "1px solid #ccc" }}>
            <button onClick={() => setCollapsed(!collapsed)}>
              {collapsed ? "展开" : "折叠"}
            </button>
            {!collapsed && (
              <ul style={{ marginTop: 10, listStyle: "none", padding: 0 }}>
                {pages.map((page) => (
                  <li key={page.id} style={{ margin: "5px 0" }}>
                    <button onClick={() => openPage(page)}>{page.title}</button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </Panel>

        <PanelResizeHandle />

        {/* 右边内容 */}
        <Panel>
          <div style={{ padding: 10, height: "100%", overflow: "auto" }}>
            {openedPages.map((page) => (
              <div key={page.id} style={{ marginBottom: 20, border: "1px solid #ccc" }}>
                <h3>{page.title}</h3>
                <iframe
                  src={page.url}
                  title={page.title}
                  style={{ width: "100%", height: 400, border: "none" }}
                />
              </div>
            ))}
          </div>
        </Panel>
      </PanelGroup>
    </div>
  );
};

export default AllStock;
