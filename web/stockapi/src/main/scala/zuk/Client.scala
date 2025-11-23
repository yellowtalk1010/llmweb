package zuk

import com.alibaba.fastjson2.JSONObject
import org.apache.commons.lang3.StringUtils

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket
import java.util.concurrent.{ExecutorService, Executors, LinkedBlockingQueue}
import scala.beans.BeanProperty

object Client {

  val xxxxxxxxx = "XXX-XXX-XXX"

  val executor: ExecutorService = Executors.newCachedThreadPool
  var socketInput: BufferedReader = null
  var socketOutput: PrintWriter = null

  val queue = new LinkedBlockingQueue[LlmStreamChat]()

  /***
   * 处理读取到的数据
   */
  def handleReadLine(line: String): Unit = {
    try {
      val obj = JSONObject.parseObject(line)
      val messageType = obj.get("messageType").asInstanceOf[String]
      val messageId = obj.get("messageId").asInstanceOf[String]
      messageType match
        case "llm/streamChat" =>
          val obj: LlmStreamChat = JSONObject.parseObject(line, classOf[LlmStreamChat])
          if(!obj.data.done){
            val content = obj.data.content.content
            println(s">>>>>>>>>>>>>>>>>>>>>>>${content}")
            queue.put(obj)
          }
        case _=>
    }
    catch
      case exception: Exception =>
  }

  def handleWriteLine(line: String): Unit = {
    try {
      println(s"write:${line}")
      socketOutput.println(line)
      socketOutput.flush()
    }
    catch
      case exception: Exception =>
  }

  /***
   * 初始化
   */
  def init(): Unit = {
    val socketClient = new Socket("127.0.0.1", 1010)
    socketInput = new BufferedReader(new InputStreamReader(socketClient.getInputStream))
    socketOutput = new PrintWriter(socketClient.getOutputStream(), true)

    executor.execute(() => {
      while (true) {
        try {
          Thread.sleep(20)
          val line = socketInput.readLine()
          if (StringUtils.isNotBlank(line)) {
            println(s"read:${line}")
            handleReadLine(line)
          }
        }
        catch
          case exception: Exception => exception.printStackTrace()
      }
    })
    println("完成LLM连接")
  }

  def testWrite(messageId: String): Unit = {
    try {
      val llm = """{"messageId":"XXX-XXX-XXX","messageType":"llm/streamChat","data":{"completionOptions":{"reasoning":false},"title":"DeepSeek Coder","messages":[{"role":"system","content":"\u003cimportant_rules\u003e\n  You are in agent mode.\n\n  If you need to use multiple tools, you can call multiple read-only tools simultaneously.\n\n  Always include the language and file name in the info string when you write code blocks.\n  If you are editing \"src/main.py\" for example, your code block should start with \u0027```python src/main.py\u0027\n\n\nFor larger codeblocks (\u003e20 lines), use brief language-appropriate placeholders for unmodified sections, e.g. \u0027// ... existing code ...\u0027\n\nHowever, only output codeblocks for suggestion and demonstration purposes, for example, when enumerating multiple hypothetical options. For implementing changes, use the edit tools.\n\n\u003c/important_rules\u003e\n\n\u003ctool_use_instructions\u003e\nYou have access to several \"tools\" that you can use at any time to retrieve information and/or perform tasks for the User.\nTo use a tool, respond with a tool code block (```tool) using the syntax shown in the examples below:\n\nThe following tools are available to you:\n\nTo read a file with a known filepath, use the read_file tool. For example, to read a file located at \u0027path/to/file.txt\u0027, you would respond with this:\n```tool\nTOOL_NAME: read_file\nBEGIN_ARG: filepath\npath/to/the_file.txt\nEND_ARG\n```\n\nTo create a NEW file, use the create_new_file tool with the relative filepath and new contents. For example, to create a file located at \u0027path/to/file.txt\u0027, you would respond with:\n```tool\nTOOL_NAME: create_new_file\nBEGIN_ARG: filepath\npath/to/the_file.txt\nEND_ARG\nBEGIN_ARG: contents\nContents of the file\nEND_ARG\n```\n\nTo run a terminal command, use the run_terminal_command tool\nThe shell is not stateful and will not remember any previous commands.      When a command is run in the background ALWAYS suggest using shell commands to stop it; NEVER suggest using Ctrl+C.      When suggesting subsequent shell commands ALWAYS format them in shell command blocks.      Do NOT perform actions requiring special/admin privileges.      Choose terminal commands and scripts optimized for win32 and x64 and shell powershell.exe.\nYou can also optionally include the waitForCompletion argument set to false to run the command in the background.      \nFor example, to see the git log, you could respond with:\n```tool\nTOOL_NAME: run_terminal_command\nBEGIN_ARG: command\ngit log\nEND_ARG\n```\n\nTo return a list of files based on a glob search pattern, use the file_glob_search tool\n```tool\nTOOL_NAME: file_glob_search\nBEGIN_ARG: pattern\n*.py\nEND_ARG\n```\n\nTo view the current git diff, use the view_diff tool. This will show you the changes made in the working directory compared to the last commit.\n```tool\nTOOL_NAME: view_diff\n```\n\nTo view the user\u0027s currently open file, use the read_currently_open_file tool.\nIf the user is asking about a file and you don\u0027t see any code, use this to check the current file\n```tool\nTOOL_NAME: read_currently_open_file\n```\n\nTo list files and folders in a given directory, call the ls tool with \"dirPath\" and \"recursive\". For example:\n```tool\nTOOL_NAME: ls\nBEGIN_ARG: dirPath\npath/to/dir\nEND_ARG\nBEGIN_ARG: recursive\nfalse\nEND_ARG\n```\n\nSometimes the user will provide feedback or guidance on your output. If you were not aware of these \"rules\", consider using the create_rule_block tool to persist the rule for future interactions.\nThis tool cannot be used to edit existing rules, but you can search in the \".continue/rules\" folder and use the edit tool to manage rules.\nTo create a rule, respond with a create_rule_block tool call and the following arguments:\n- name: Short, descriptive name summarizing the rule\u0027s purpose (e.g. \u0027React Standards\u0027, \u0027Type Hints\u0027)\n- rule: Clear, imperative instruction for future code generation (e.g. \u0027Use named exports\u0027, \u0027Add Python type hints\u0027). Each rule should focus on one specific standard.\n- description: Description of when this rule should be applied. Required for Agent Requested rules (AI decides when to apply). Optional for other types.\n- globs: Optional file patterns to which this rule applies (e.g. [\u0027**/*.{ts,tsx}\u0027] or [\u0027src/**/*.ts\u0027, \u0027tests/**/*.ts\u0027])\n- alwaysApply: Whether this rule should always be applied. Set to false for Agent Requested and Manual rules. Omit or set to true for Always and Auto Attached rules.\nFor example:\n```tool\nTOOL_NAME: create_rule_block\nBEGIN_ARG: name\nUse PropTypes\nEND_ARG\nBEGIN_ARG: rule\nAlways use PropTypes when declaring React component properties\nEND_ARG\nBEGIN_ARG: description\nEnsure that all prop types are explicitly declared for better type safety and code maintainability in React components.\nEND_ARG\nBEGIN_ARG: globs\n**/*.js\nEND_ARG\nBEGIN_ARG: alwaysApply\nfalse\nEND_ARG\n```\n\nTo fetch the content of a URL, use the fetch_url_content tool. For example, to read the contents of a webpage, you might respond with:\n```tool\nTOOL_NAME: fetch_url_content\nBEGIN_ARG: url\nhttps://example.com\nEND_ARG\n```\n\nTo edit an EXISTING file, use the edit_existing_file tool with\n- filepath: the relative filepath to the file.\n- changes: Any modifications to the file, showing only needed changes. Do NOT wrap this in a codeblock or write anything besides the code changes. In larger files, use brief language-appropriate placeholders for large unmodified sections, e.g. \u0027// ... existing code ...\u0027\nOnly use this tool if you already know the contents of the file. Otherwise, use the read_file or read_currently_open_file tool to read it first.\nFor example:\n```tool\nTOOL_NAME: edit_existing_file\nBEGIN_ARG: filepath\npath/to/the_file.ts\nEND_ARG\nBEGIN_ARG: changes\n// ... existing code ...\nfunction subtract(a: number, b: number): number {\n  return a - b;\n}\n// ... rest of code ...\nEND_ARG\n```\n\nTo perform exact string replacements in files, use the single_find_and_replace tool with a filepath (relative to the root of the workspace) and the strings to find and replace.\n\n  For example, you could respond with:\n```tool\nTOOL_NAME: single_find_and_replace\nBEGIN_ARG: filepath\npath/to/file.ts\nEND_ARG\nBEGIN_ARG: old_string\nconst oldVariable \u003d \u0027value\u0027\nEND_ARG\nBEGIN_ARG: new_string\nconst newVariable \u003d \u0027updated\u0027\nEND_ARG\nBEGIN_ARG: replace_all\nfalse\nEND_ARG\n```\n\nTo perform a grep search within the project, call the grep_search tool with the query pattern to match. For example:\n```tool\nTOOL_NAME: grep_search\nBEGIN_ARG: query\n.*main_services.*\nEND_ARG\n```\n\nIf it seems like the User\u0027s request could be solved with one of the tools, choose the BEST one for the job based on the user\u0027s request and the tool descriptions\nThen send the ```tool codeblock (YOU call the tool, not the user). Always start the codeblock on a new line.\nDo not perform actions with/for hypothetical files. Ask the user or use tools to deduce which files are relevant.\nYou can only call ONE tool at at time. The tool codeblock should be the last thing you say; stop your response after the tool codeblock.\n\u003c/tool_use_instructions\u003e"},{"role":"user","content":"\n```kt test.kt (4-7)\n    fun add(number: Double): Test {\n        result +\u003d number\n        return this\n    }\n```\n\nanalysis  code."},{"role":"assistant","content":"分析C/C++代码，让代码没有缺陷和漏洞，更好更安全。\n\n\n"},{"role":"user","content":"UINT_32 new = 0; \n\n\n\n"},{"role":"user","content":"分析这段代码，中文回复"}],"messageOptions":{"precompiled":true}}}"""
      val l = llm.replaceAll(xxxxxxxxx, messageId)
      handleWriteLine(l)
    }
    catch
      case exception: Exception =>
  }


  case class ContentData(@BeanProperty role: String,
                    @BeanProperty content: String)

  case class Data(@BeanProperty done: Boolean,
              @BeanProperty content: ContentData,
              @BeanProperty status: String)

  case class LlmStreamChat(@BeanProperty messageType: String,
                      @BeanProperty data: Data,
                      @BeanProperty messageId: String)
}
