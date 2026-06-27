package zuk.token.providers.deepseek.tasks

import zuk.token.providers.ITask

import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

/***
 输入参数：


 提取 https://emweb.securities.eastmoney.com/pc_hsf10/pages/index.html?type=web&code=SH688323&color=b#/hxtc/tcxq 里面的板块和概念。输出的格式严格采用如下格式：
```
##########
 股票分析结果如下：
 股票代码：填写股票的代码
 股票名称：填写股票的名称
 概念：概念1，概念2
 一级行业：行业1
 二级行业：行业2
 三级行业: 行业3
 ##########
```
不需要附加任何其他信息的补充说明。



 输出参数：
 ##########
 股票分析结果如下：
 股票代码：SH688323
 股票名称：瑞华泰
 概念：电池技术，储能概念，固态电池，商业航天，PCB，华为概念，风能，深圳特区
 一级行业：基础化工
 二级行业：塑料
 三级行业：膜材料
 ##########


 */

/***
 * @param chatContent
 */
class DeepseekTask_easymoneyConcept extends DeepseekTask {

  val stockCode: String = ""
  val stockName: String = ""
  val stockConceptURL: String = ""
  //https://emweb.securities.eastmoney.com/pc_hsf10/pages/index.html?type=web&code=SH688323&color=b#/hxtc/tcxq

  def createPrompt(): String = {
    s"""
      |提取 ${stockConceptURL} 里面的板块和概念。输出的格式严格采用如下格式：
      |```
      |##########
      | 股票分析结果如下：
      | 股票代码：填写股票的代码
      | 股票名称：填写股票的名称
      | 概念：概念1，概念2
      | 一级行业：行业1
      | 二级行业：行业2
      | 三级行业: 行业3
      | ##########
      |```
      |不需要附加任何其他信息的补充说明。
      |""".stripMargin
  }

  override def checkResult(): Boolean = {
    val parseResult = this.parseProvider()
    val lines = parseResult.split("\n").toList
    lines.filter(_.contains(this.stockCode)).size>0
      && lines.filter(_.contains(this.stockName)).size>0
  }

}
