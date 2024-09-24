package com.argonet.practice.mapper;

import com.argonet.practice.vo.AffiliationVO;
import com.argonet.practice.vo.AuthorVO;
import com.argonet.practice.vo.MeshHeadingVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestMapper {

    int insertAffiliation(List<AffiliationVO> affiliationVOList);

    int insertAuthor(List<AuthorVO> authorVOList);

    int insertMeshHeading(List<MeshHeadingVO> meshHeadingVOList);

    String getTable();
}
