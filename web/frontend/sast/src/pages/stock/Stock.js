import './../../index.css';
import { useEffect, useRef, useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";

/***
 * 股票分析系统前端展示首页
 */
function Stock() {
  // 从接口获取页面列表
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const leftPanelRef = useRef(null);
  const [collapsed, setCollapsed] = useState(false);
  const [isHovering, setIsHovering] = useState(false);

  // openedPages: [{ instanceKey, pageId, title, url }]
  const [openedPages, setOpenedPages] = useState([]);
  const [activeKey, setActiveKey] = useState(null);
  const [iframeSrcMap, setIframeSrcMap] = useState({});

  // 用于生成实例 key 的自增计数器（useRef 避免闭包问题）
  const instanceCounterRef = useRef(0);

  // 组件加载时请求接口
  useEffect(() => {
    const controller = new AbortController();

    const fetchPages = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await fetch("/pages/list", {
          method: "GET",
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`请求失败：HTTP ${response.status}`);
        }

        const data = await response.json();

        const pageList = Array.isArray(data) ? data : data.data;

        if (!Array.isArray(pageList)) {
          throw new Error("接口返回的数据格式不正确");
        }

        setPages(pageList);
      } catch (err) {
        if (err.name !== "AbortError") {
          console.error("获取页面列表失败：", err);
          setError(err.message || "获取页面列表失败");
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    fetchPages();

    return () => controller.abort();
  }, []);

  const toggleLeftPanel = (e) => {
    e.stopPropagation();

    if (collapsed) {
      leftPanelRef.current?.expand();
    } else {
      leftPanelRef.current?.collapse();
    }

    setCollapsed(!collapsed);
  };

  // 每次点击都新建一个实例，允许重复打开同一个页面
  const openPage = (page) => {
    const instanceKey = `page-${page.id}-${++instanceCounterRef.current}`;

    setOpenedPages((prev) => [
      ...prev,
      { instanceKey, pageId: page.id, title: page.title, url: page.url },
    ]);

    setIframeSrcMap((prev) => ({
      ...prev,
      [instanceKey]: page.url,
    }));

    setActiveKey(instanceKey);
  };

  const closePage = (instanceKey) => {
    setOpenedPages((prev) => {
      const remaining = prev.filter((p) => p.instanceKey !== instanceKey);

      if (activeKey === instanceKey) {
        const closedIndex = prev.findIndex(
          (p) => p.instanceKey === instanceKey
        );
        const nextPage =
          remaining[Math.min(closedIndex, remaining.length - 1)];

        setActiveKey(nextPage?.instanceKey ?? null);
      }

      return remaining;
    });

    setIframeSrcMap((prev) => {
      const newMap = { ...prev };
      delete newMap[instanceKey];
      return newMap;
    });
  };

  const refreshPage = (instanceKey) => {
    setIframeSrcMap((prev) => {
      const src = prev[instanceKey];
      if (!src) return prev;

      const url = new URL(src, window.location.href);
      url.searchParams.set("_refresh", Date.now());

      return {
        ...prev,
        [instanceKey]: url.href,
      };
    });
  };

  return (
    <PanelGroup direction="horizontal" style={{ height: "100vh" }}>
      <Panel
        ref={leftPanelRef}
        collapsible
        defaultSize={10}
        minSize={5}
      >
        {loading ? (
          <div style={{ padding: 10 }}>正在加载页面列表...</div>
        ) : error ? (
          <div style={{ padding: 10, color: "red" }}>
            {error}
            <button
              onClick={() => window.location.reload()}
              style={{ marginLeft: 8 }}
            >
              重试
            </button>
          </div>
        ) : (
          <ul style={{ margin: 0, padding: 10, listStyle: "none" }}>
            {pages.map((page) => (
              <li key={page.id} style={{ margin: "8px 0" }}>
                <a
                  href="#!"
                  onClick={(e) => {
                    e.preventDefault();
                    openPage(page);
                  }}
                  style={{
                    textDecoration: "none",
                    color: "#007bff",
                    cursor: "pointer",
                  }}
                >
                  {page.title}
                </a>
              </li>
            ))}
          </ul>
        )}
      </Panel>

      <PanelResizeHandle
        style={{
          width: "12px",
          backgroundColor: isHovering ? "#e5e7eb" : "#f3f4f6",
          position: "relative",
          transition: "background-color 0.2s",
        }}
        onMouseEnter={() => setIsHovering(true)}
        onMouseLeave={() => setIsHovering(false)}
      >
        <div
          onClick={toggleLeftPanel}
          style={{
            position: "absolute",
            left: "50%",
            top: "50%",
            transform: "translate(-50%, -50%)",
            cursor: "pointer",
            padding: "4px",
            borderRadius: "4px",
            backgroundColor: isHovering ? "#d1d5db" : "transparent",
          }}
        >
          {collapsed ? "▶️" : "◀️"}
        </div>
      </PanelResizeHandle>

      <Panel defaultSize={90}>
        <div
          style={{
            height: "100%",
            display: "flex",
            flexDirection: "column",
          }}
        >
          {/* 标签页 */}
          {openedPages.length > 0 && (
            <div
              style={{
                display: "flex",
                borderBottom: "1px solid #ccc",
              }}
            >
              {openedPages.map((page) => (
                <div
                  key={page.instanceKey}
                  className={`px-3 py-1 cursor-pointer border-r flex items-center ${
                    page.instanceKey === activeKey
                      ? "bg-green-100"
                      : "bg-white"
                  }`}
                >
                  <span onClick={() => setActiveKey(page.instanceKey)}>
                    {page.title}
                  </span>

                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      refreshPage(page.instanceKey);
                    }}
                    style={{
                      marginLeft: 5,
                      border: "none",
                      background: "transparent",
                      cursor: "pointer",
                    }}
                    title="刷新页面"
                  >
                    🔄
                  </button>

                  <button
                    onClick={() => closePage(page.instanceKey)}
                    style={{
                      marginLeft: 5,
                      border: "none",
                      background: "transparent",
                      cursor: "pointer",
                    }}
                    title="关闭页面"
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* iframe 区域 */}
          <div style={{ flex: 1, position: "relative" }}>
            {openedPages.map((page) => (
              <iframe
                key={page.instanceKey}
                src={iframeSrcMap[page.instanceKey]}
                title={page.title}
                style={{
                  display:
                    page.instanceKey === activeKey ? "block" : "none",
                  width: "100%",
                  height: "100%",
                  border: "none",
                  position: "absolute",
                  top: 0,
                  left: 0,
                }}
              />
            ))}

            {openedPages.length === 0 && (
              <div style={{ padding: 20 }}>
                {loading
                  ? "正在加载页面列表..."
                  : "请选择一个网页打开"}
              </div>
            )}
          </div>
        </div>
      </Panel>
    </PanelGroup>
  );
}

export default Stock;