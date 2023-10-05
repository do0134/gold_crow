import React from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";

import Header from "../../components/Header";

const Intro = () => {
  const navigate = useNavigate();
  const isLoggedIn = useSelector((state) => state.user.value.isLoggedIn);

  const createTeamHandler = () => {
    if (isLoggedIn) {
      navigate("/teams/create");
    } else {
      alert("로그인 필요");
      navigate("/login");
    }
  };

  return (
    <React.Fragment>
      <div className="flex flex-col h-screen">
        <Header />
        <div
          data-aos="fade-in"
          className="flex flex-wrap h-full p-4 justify-center justify-items-center items-center"
        >
          <div className="h-fit flex flex-col items-center">
            <img
              className="w-20 rounded-full"
              src={require("../../assets/images/logo.png")}
              alt="logo-img"
            />
            <div className="text-7xl font-bold text-center text-white mt-5 tracking-wider">
              까마귀공방
            </div>
            <div className="text-center text-white flex-col mt-11">
              <div className="text-4xl">무언가 굉장하고 엄청난</div>
              <div className="text-4xl">웹IDE계의 이단아가 등장했다</div>
              <div className="text-4xl">다들 사용해달라 지금 당장!!</div>
            </div>
            <button
              onClick={createTeamHandler}
              className="w-72 h-12 mt-14 text-xl font-bold bg-point_light_yellow text-component_dark hover:bg-point_yellow rounded-md transition"
            >
              팀 생성하기
            </button>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Intro;
