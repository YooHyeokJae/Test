package com.argonet.practice.service;

import com.argonet.practice.mapper.TestMapper;
import com.argonet.practice.vo.AffiliationVO;
import com.argonet.practice.vo.AuthorVO;
import com.argonet.practice.vo.MeshHeadingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    @Autowired
    TestMapper testMapper;

    public int insertAffiliation(List<AffiliationVO> affiliationVOList) {
        return testMapper.insertAffiliation(affiliationVOList);
    }

    public int insertAuthor(List<AuthorVO> authorVOList) {
        return testMapper.insertAuthor(authorVOList);
    }

    public int insertMeshHeading(List<MeshHeadingVO> meshHeadingVOList) {
        return testMapper.insertMeshHeading(meshHeadingVOList);
    }

    public String getTable() {
        return this.testMapper.getTable();
    }
}
