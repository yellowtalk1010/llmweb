package zuk

import org.apache.commons.codec.digest.DigestUtils
import org.scalatest.funsuite.AnyFunSuite

import java.io.{File, FileInputStream, IOException}
import java.nio.file.{Files, Path, Paths}
import java.util
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*

class ClassCompareTest extends AnyFunSuite {

  @throws[IOException]
  def getAllFiles(path: String): java.util.List[Path] = {
    val rootPath = Paths.get(path)
    val paths = Files.walk(rootPath)
    val ls = paths.toList.asScala.filter(p=>p.toFile.isFile).map(p=>{
      val relativizePath = rootPath.relativize(p)
      val str = relativizePath.toString
      relativizePath
    }).toList.sortBy(e=>(e.toString)).asJava
    ls
  }

  test("class比较") {
    val jarClassPath = "C:\\Users\\5132\\.m2\\repository\\zuk\\cbsast\\merge-93\\2.0.0\\merge-93-2.0.0\\cn"
    val jarClassPathFiles = getAllFiles(jarClassPath)
    println(s"${jarClassPathFiles.size()}")
    val jarClassPathMap = new util.HashMap[String, Path]()
    jarClassPathFiles.asScala.foreach(p=>{
      jarClassPathMap.put(p.toString, p)
    })

    val targetClassPath = "D:\\development\\github\\webb\\web\\cobot-parsers\\target\\classes\\cn"
    val targetClassPathFiles = getAllFiles(targetClassPath)
    println(s"${targetClassPathFiles.size()}")
    val targetClassPathMap = new util.HashMap[String, Path]()
    targetClassPathFiles.asScala.foreach(p=>{
      targetClassPathMap.put(p.toString, p)
    })

    val num = new AtomicInteger(0)
    val num1 = new AtomicInteger(0)
    val num2 = new AtomicInteger(0)
    jarClassPathFiles.asScala.filter(p=> !p.toString.contains("utils\\linetracker")  ).filter(p=>{
      this.ignoreFiles().filter(f=>f.endsWith(p.toString)).size == 0
    }).foreach(p=>{
      val path = targetClassPathMap.get(p.toString)
      if(path==null){
        println(s"${num.addAndGet(1)}缺少文件：cn\\${p.toString}")
      }
      else {
        val p1 = jarClassPath + "\\" + p.toString
        val fis1 = new FileInputStream(p1)
        val md5_1 = DigestUtils.md5Hex(fis1)

        val p2 = targetClassPath  + "\\" + path.toString
        val fis2 = new FileInputStream(p2)
        val md5_2 = DigestUtils.md5Hex(fis2)


        if(!md5_1.equals(md5_2)){
          println(s"${num1.addAndGet(1)}文件不相同：${p2}")
          val javaPath = if(p2.contains("$")){
            s"D:\\development\\github\\webb\\web\\cobot-parsers\\src\\main\\java\\cn\\${path.toString.split("\\$")(0)}.java"
          }
          else {
            s"D:\\development\\github\\webb\\web\\cobot-parsers\\src\\main\\java\\cn\\${path.toString.replaceAll(".class",".java")}"
          }
//          val javaPath = s"D:\\development\\github\\webb\\web\\cobot-parsers\\src\\main\\java\\cn\\${path.toString.replaceAll(".class",".java")}"
          if(new File(javaPath).exists()){
            println(s"           ${javaPath}，${new File(javaPath).exists()}")
//            new File(javaPath).delete()
          }
        }
        else {
          //println(s"${num2.addAndGet(1)}文件相同")
        }
      }
    })

    println()
  }

  private def ignoreFiles(): Set[String] = {
    Set[String](
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\model\\CProject.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMProject.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\ICProject.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\ICModelMarker.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\CoreModelUtil.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\CoreModel.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\CModelException.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\AssemblyLanguage.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\ICModelMarker.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\CoreModelUtil.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\CoreModel.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\CModelException.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\AssemblyLanguage.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\language\\settings\\providers\\ScannerDiscoveryLegacySupport.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\language\\settings\\providers\\LanguageSettingsSerializableProvider.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\language\\settings\\providers\\LanguageSettingsManager.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\language\\settings\\providers\\LanguageSettingsBaseProvider.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\language\\ProjectLanguageConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\provider\\ReadOnlyPDOMProviderBridge.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\provider\\IIndexFragmentProvider.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\provider\\IIndexProvider.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\export\\ExportIndexFileInfoMatcher.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\export\\ExternalExportProjectProvider$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\export\\ExternalExportProjectProvider.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\export\\AbstractExportProjectProvider.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\URIRelativeLocationConverter.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\ResourceContainerRelativeLocationConverter.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IndexLocationFactory.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IndexFilter$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IndexFilter$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IndexFilter.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMASTProcessorDesc$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMASTProcessorDesc.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IPDOMASTProcessor$Abstract.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IPDOMASTProcessor.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$3.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$4.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$5.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$6$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$6.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$7$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$7.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$8.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$9$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$9.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager$PCL.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\PDOMManager.class",   /// XXX，变动很大，是不是版本不对？
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IIndexName.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\IIndexFragmentName.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\dom\\PDOMMacroDefinitionName.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\dom\\PDOMMacroReferenceName.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\dom\\PDOMName.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\dom\\PDOMMacro.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\WritableCIndex.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\IndexFileSet.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\IndexFileSet$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IIndexMacro.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IIndexFileSet.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\dom\\PDOMFile$Comparator.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\dom\\PDOMFile$Finder.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\pdom\\dom\\PDOMFile.class", //XXX 仅完成一半
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\IIndexFragmentFile.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IIndexFile.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IIndexBinding.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\EmptyCIndex.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\IWritableIndex.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\IWritableIndex$IncludeInformation.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\IWritableIndex.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\CIndex$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\CIndex$NameKey.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\index\\CIndex.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\index\\IIndex.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\formatter\\DefaultCodeFormatterConstants.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\formatter\\DefaultCodeFormatterOptions.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\formatter\\IndentManipulation.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\errorparsers\\ErrorParserNamedWrapper.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\errorparsers\\ErrorPattern.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\errorparsers\\RegexErrorPattern.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\rewrite\\MacroExpansionExplorer.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\rewrite\\MacroExpansionExplorer$IMacroExpansionStep.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\rewrite\\ASTRewrite.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\rewrite\\ASTRewrite$Operation.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\rewrite\\ASTRewrite$CommentPosition.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\rewrite\\ASTRewrite$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\cpp\\AbstractCPPParserExtensionConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\cpp\\GPPParserExtensionConfiguration.class",  //XXX
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\cpp\\GPPScannerExtensionConfiguration.class", //XXX
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\cpp\\POPCPPParserExtensionConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\cpp\\POPCPPScannerExtensionConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\c\\GCCParserExtensionConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\c\\GCCScannerExtensionConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\ISourceCodeParser.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\GNUScannerExtensionConfiguration$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\GNUScannerExtensionConfiguration$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\GNUScannerExtensionConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\IScannerExtensionConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\AbstractScannerExtensionConfiguration$MacroDefinition.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\AbstractScannerExtensionConfiguration.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\model\\AbstractLanguage.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\AbstractCLikeLanguage$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\AbstractCLikeLanguage$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\AbstractCLikeLanguage$NameCollector.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\parser\\AbstractCLikeLanguage.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\xml\\XMLElement.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\xml\\XMLEntityResolver.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\xml\\XMLFactoryWithLocator.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\xml\\XMLFileLocation.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\xml\\XMLSAXContentHandler.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\xml\\XMLSAXReader.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\xml\\XMLUnit.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\make\\MakeFileLocation.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\gnu\\cpp\\ICPPASTKnRFunctionDeclarator.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\gnu\\cpp\\IGPPASTArrayRangeDesignator.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\gnu\\c\\GCCLanguage.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\gnu\\cpp\\GPPLanguage.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPNodeFactory.class",   //XXX
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPExternalFunction.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPExternalBinding.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPASTNewExpression.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPASTForStatement.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPASTFieldDesignator.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPASTDesignator.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPASTDesignatedInitializer.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPASTArrayDesignator.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\cpp\\ICPPASTArraySubscriptExpression.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\c\\ICField.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\c\\ICParameter.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\c\\ICVariable.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\c\\ICExternalFunction.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\c\\ICExternalBinding.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IValue.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IType.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\ITypedef.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\cpp\\CPPScope$CPPScopeProblem.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\cpp\\CPPScope.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\c\\CScope$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\c\\CScope$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\c\\CScope$CollectNamesAction.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\c\\CScope.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IScope.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IScope$ScopeLookupData.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IProblemBinding.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\INodeFactory.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IMacroBinding.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IExternalFunction.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IExternalBinding.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IBinding.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTTypeId.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\cpp\\CPPASTTranslationUnit.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\c\\CASTTranslationUnit.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\ASTTranslationUnit.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\dom\\parser\\ASTTranslationUnit$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTTranslationUnit.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTTranslationUnit$IDependencyTree.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTTranslationUnit$IDependencyTree$IASTInclusionNode.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTPreprocessorMacroDefinition.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTNode.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTNode$CopyStyle.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTName.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTIdExpression.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTFunctionDefinition.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTFunctionDeclarator.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTFileLocation.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTNodeLocation.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTEnumerationSpecifier$IASTEnumerator.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTEnumerationSpecifier.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\core\\parser\\scanner\\ASTComment.class", //XXX 从这里开始下面，是纯逆向
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\IASTComment.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\ASTTypeUtil$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\ASTTypeUtil.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\CountedBinding.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\ASTVisitor.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\ASTNameCollector.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\ast\\ASTNodeProperty.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\IPDOMManager.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\IPDOMIndexer.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\IPDOMIndexerTask.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\IASTServiceProvider$UnsupportedDialectException.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\IASTServiceProvider.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\dom\\CDOM.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\cdtvariables\\CdtVariableStatus.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\ToolFactory.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\ProblemMarkerInfo.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\ErrorParserManager$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\ErrorParserManager.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CommandLauncher.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CProjectNature.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CCorePreferenceConstants.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CCorePlugin$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CCorePlugin$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CCorePlugin$NullConsole$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CCorePlugin$NullConsole.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CCorePlugin.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\GASErrorParser.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$3.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$4.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$5.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$6.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$7.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$8.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$9.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$10.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$11.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$12.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$13.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$14.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$15.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$16.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$17.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$18.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$19.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$20.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$21.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$22.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$23.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$24.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$25.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$26.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$27.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$28.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$29.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser$30.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\MakeErrorParser.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\VCErrorParser.class",
      "cn\\net\\cobot\\parsers\\cparser\\internal\\errorparsers\\VCErrorParser$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ParserUtility.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\MakeFileParser.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CXXCheckParamConverter.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CheckConfig.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CheckParam$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CheckParam$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CheckParam.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CobotParserConfig.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$BitFieldInterpretation.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$CharInterpretation.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$IntrinsicType_PtrdiffT.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$IntrinsicType_SizeT.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$IntrinsicType_WcharT.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$RightShiftBehaviour.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig$TypeSizeAndAlignment.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\CompilerConfig.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\LostFileHeader.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$1.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$2.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$3.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$4.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage$5.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\ProgrammingLanguage.class",
      "cn\\net\\cobot\\parsers\\cparser\\common\\TypeCompare.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\AbstractCExtension.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CCProjectNature.class",
      "cn\\net\\cobot\\parsers\\cparser\\core\\CConventions.class"
    )
  }

}
