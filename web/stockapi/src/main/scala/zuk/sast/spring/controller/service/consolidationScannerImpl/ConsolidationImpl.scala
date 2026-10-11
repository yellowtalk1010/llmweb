package zuk.sast.spring.controller.service.consolidationScannerImpl

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import zuk.consolidation.ConsolidationScanner

/***
 * 黏合递增
 */
@Service
class ConsolidationImpl {
  
  @PostConstruct
  def init() = {
    ConsolidationScanner.main(Array.empty)
  }

}
